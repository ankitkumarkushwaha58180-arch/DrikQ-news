package com.example.data.firebase

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.data.bunny.BunnyStorageHelper
import com.example.data.model.AppPolicy
import com.example.data.model.MediaType
import com.example.data.model.Post
import com.example.data.model.PostStatus
import com.example.data.model.PushNotificationItem
import com.example.data.model.Reporter
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

object FirebaseRepository {
    private const val TAG = "FirebaseRepository"
    const val RTDB_BASE_URL = "https://drikq-f9a39-default-rtdb.asia-southeast1.firebasedatabase.app"
    private const val PREFS_NAME = "drikq_prefs"

    private val httpClient = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var sharedPrefs: SharedPreferences? = null

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _currentReporter = MutableStateFlow<Reporter?>(null)
    val currentReporter: StateFlow<Reporter?> = _currentReporter.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _reporters = MutableStateFlow<List<Reporter>>(emptyList())
    val reporters: StateFlow<List<Reporter>> = _reporters.asStateFlow()

    private val _policy = MutableStateFlow(
        AppPolicy(
            privacyPolicy = "Drikq News is committed to protecting your privacy. We collect minimal device information and authentication tokens solely to provide real-time news delivery and reporter attribution. Content uploaded by verified reporters undergoes editorial approval before publishing to our public feed. We do not sell or share personal user data with third-party advertisers.",
            termsAndConditions = "By using Drikq News, you agree to access verified news content responsibly. Reporters must verify facts before submission. Content violating local laws, promoting hate speech, or containing unauthorized private media will be promptly removed and credentials revoked."
        )
    )
    val policy: StateFlow<AppPolicy> = _policy.asStateFlow()

    private val _notifications = MutableStateFlow<List<PushNotificationItem>>(emptyList())
    val notifications: StateFlow<List<PushNotificationItem>> = _notifications.asStateFlow()

    private val _adsEnabled = MutableStateFlow(true)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled.asStateFlow()

    private var isListeningSettings = false
    private val viewedPostIds = mutableSetOf<String>()

    /**
     * Initializes persistence with Android SharedPreferences and checks FirebaseAuth currentUser.
     */
    fun initPersistence(context: Context) {
        if (sharedPrefs != null) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs = prefs

        // 1. Check FirebaseAuth first
        val fbUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }

        val savedUid = prefs.getString("user_uid", null)
        val savedEmail = prefs.getString("user_email", null)
        val savedName = prefs.getString("user_name", null)
        val savedPhoto = prefs.getString("user_photo", "") ?: ""

        if (fbUser != null && !fbUser.email.isNullOrBlank()) {
            val user = UserProfile(
                uid = fbUser.uid,
                name = fbUser.displayName ?: fbUser.email!!.substringBefore("@"),
                email = fbUser.email!!,
                photoUrl = fbUser.photoUrl?.toString() ?: savedPhoto
            )
            _currentUser.value = user
        } else if (!savedUid.isNullOrBlank() && !savedEmail.isNullOrBlank()) {
            val user = UserProfile(
                uid = savedUid,
                name = savedName ?: savedEmail.substringBefore("@"),
                email = savedEmail,
                photoUrl = savedPhoto
            )
            _currentUser.value = user
        }

        val savedAds = prefs.getBoolean("ads_enabled", true)
        _adsEnabled.value = savedAds

        fetchFromRemoteRtdb()
        startSettingsRealtimeListener()
    }

    private fun getUserId(): String {
        return _currentUser.value?.uid ?: sharedPrefs?.getString("user_uid", null) ?: "guest"
    }

    fun fetchFromRemoteRtdb() {
        scope.launch {
            try {
                val currentUid = getUserId()
                val locallyLiked = sharedPrefs?.getStringSet("liked_posts_$currentUid", emptySet()) ?: emptySet()
                val locallyFollowed = sharedPrefs?.getStringSet("followed_reps_$currentUid", emptySet()) ?: emptySet()

                // Fetch adsEnabled setting
                try {
                    val adsReq = Request.Builder()
                        .url("$RTDB_BASE_URL/settings/adsEnabled.json")
                        .get()
                        .build()
                    httpClient.newCall(adsReq).execute().use { adsRes ->
                        if (adsRes.isSuccessful) {
                            val adsBody = adsRes.body?.string()?.trim()
                            if (!adsBody.isNullOrBlank() && adsBody != "null") {
                                val enabled = adsBody == "true" || adsBody == "\"true\""
                                _adsEnabled.value = enabled
                                sharedPrefs?.edit()?.putBoolean("ads_enabled", enabled)?.apply()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error fetching adsEnabled: ${e.message}")
                }

                // Fetch policy
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/settings/policy.json")
                    .get()
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank() && body != "null") {
                            val json = JSONObject(body)
                            _policy.value = AppPolicy(
                                privacyPolicy = json.optString("privacyPolicy", _policy.value.privacyPolicy),
                                termsAndConditions = json.optString("termsAndConditions", _policy.value.termsAndConditions)
                            )
                        }
                    }
                }

                // Fetch user follows
                val followsFromRtdb = mutableSetOf<String>()
                if (currentUid != "guest") {
                    try {
                        val fReq = Request.Builder()
                            .url("$RTDB_BASE_URL/follows/$currentUid.json")
                            .get()
                            .build()
                        httpClient.newCall(fReq).execute().use { fRes ->
                            if (fRes.isSuccessful) {
                                val fBody = fRes.body?.string()
                                if (!fBody.isNullOrBlank() && fBody != "null") {
                                    val fJson = JSONObject(fBody)
                                    val keys = fJson.keys()
                                    while (keys.hasNext()) {
                                        val k = keys.next()
                                        if (fJson.optBoolean(k, false) || fJson.optString(k) == "true") {
                                            followsFromRtdb.add(k)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                val allFollowedReporters = followsFromRtdb + locallyFollowed

                // Fetch real reporters from RTDB
                val repReq = Request.Builder()
                    .url("$RTDB_BASE_URL/reporters.json")
                    .get()
                    .build()
                httpClient.newCall(repReq).execute().use { repResponse ->
                    if (repResponse.isSuccessful) {
                        val body = repResponse.body?.string()
                        if (!body.isNullOrBlank() && body != "null") {
                            val json = JSONObject(body)
                            val list = mutableListOf<Reporter>()
                            val keys = json.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val obj = json.optJSONObject(key)
                                if (obj != null) {
                                    val repId = obj.optString("id", key)
                                    val isFollowed = allFollowedReporters.contains(repId)
                                    val followedList = if (isFollowed) mutableListOf(currentUid) else mutableListOf()
                                    list.add(
                                        Reporter(
                                            id = repId,
                                            name = obj.optString("name"),
                                            mobile = obj.optString("mobile"),
                                            address = obj.optString("address"),
                                            photoUrl = obj.optString("photoUrl"),
                                            password = obj.optString("password"),
                                            followersCount = obj.optInt("followersCount", 0),
                                            followingCount = obj.optInt("followingCount", 0),
                                            followedByUsers = followedList
                                        )
                                    )
                                }
                            }
                            _reporters.value = list
                        }
                    }
                }

                // Fetch posts from RTDB
                val postsReq = Request.Builder()
                    .url("$RTDB_BASE_URL/posts.json")
                    .get()
                    .build()
                httpClient.newCall(postsReq).execute().use { postResponse ->
                    if (postResponse.isSuccessful) {
                        val body = postResponse.body?.string()
                        if (!body.isNullOrBlank() && body != "null") {
                            val json = JSONObject(body)
                            val list = mutableListOf<Post>()
                            val keys = json.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val obj = json.optJSONObject(key)
                                if (obj != null) {
                                    val postId = obj.optString("id", key)
                                    val statusStr = obj.optString("status", "PENDING").uppercase()
                                    val mediaTypeStr = obj.optString("mediaType", "VIDEO").uppercase()

                                    // Likes persistence parser
                                    val likedUsersList = mutableListOf<String>()
                                    val likesObj = obj.optJSONObject("likes")
                                    if (likesObj != null) {
                                        val lKeys = likesObj.keys()
                                        while (lKeys.hasNext()) {
                                            val uKey = lKeys.next()
                                            if (likesObj.optBoolean(uKey, false) || likesObj.optString(uKey) == "true") {
                                                likedUsersList.add(uKey)
                                            }
                                        }
                                    }
                                    if (locallyLiked.contains(postId) && !likedUsersList.contains(currentUid)) {
                                        likedUsersList.add(currentUid)
                                    }

                                    list.add(
                                        Post(
                                            id = postId,
                                            title = obj.optString("title"),
                                            description = obj.optString("description"),
                                            place = obj.optString("place"),
                                            mediaType = if (mediaTypeStr == "PHOTO") MediaType.PHOTO else MediaType.VIDEO,
                                            mediaUrl = obj.optString("mediaUrl"),
                                            thumbnailUrl = obj.optString("thumbnailUrl", obj.optString("mediaUrl")),
                                            reporterId = obj.optString("reporterId"),
                                            reporterName = obj.optString("reporterName"),
                                            reporterPhotoUrl = obj.optString("reporterPhotoUrl"),
                                            status = if (statusStr == "APPROVED") PostStatus.APPROVED else PostStatus.PENDING,
                                            likesCount = obj.optInt("likesCount", likedUsersList.size),
                                            viewsCount = obj.optInt("viewsCount", 0),
                                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                                            likedByUsers = likedUsersList
                                        )
                                    )
                                }
                            }
                            _posts.value = list.sortedByDescending { it.timestamp }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from RTDB: ${e.message}")
            }
        }
    }

    fun loginWithGoogle(email: String, displayName: String, photoUrl: String) {
        val uid = "usr_" + UUID.randomUUID().toString().take(8)
        val user = UserProfile(
            uid = uid,
            name = displayName,
            email = email,
            photoUrl = photoUrl
        )
        _currentUser.value = user

        // Persist session in SharedPreferences
        sharedPrefs?.edit()?.apply {
            putString("user_uid", user.uid)
            putString("user_email", user.email)
            putString("user_name", user.name)
            putString("user_photo", user.photoUrl)
            apply()
        }

        fetchFromRemoteRtdb()
    }

    fun logoutUser() {
        _currentUser.value = null
        sharedPrefs?.edit()?.apply {
            remove("user_uid")
            remove("user_email")
            remove("user_name")
            remove("user_photo")
            apply()
        }
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
    }

    fun authenticateReporter(userIdInput: String, passwordInput: String): Reporter? {
        val trimmedId = userIdInput.trim()
        val trimmedPass = passwordInput.trim()
        val reporter = _reporters.value.find {
            it.id.equals(trimmedId, ignoreCase = true) && it.password == trimmedPass
        }
        if (reporter != null) {
            _currentReporter.value = reporter
        }
        return reporter
    }

    fun logoutReporter() {
        _currentReporter.value = null
    }

    /**
     * Exact required code: 274813
     */
    fun loginAdmin(passcode: String): Boolean {
        return if (passcode.trim() == "274813") {
            _isAdminLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    suspend fun uploadPostWithBunny(
        context: Context,
        fileUri: Uri,
        title: String,
        description: String,
        place: String,
        mediaType: MediaType,
        reporter: Reporter,
        onProgress: (Int) -> Unit
    ): Result<Post> {
        val ext = if (mediaType == MediaType.VIDEO) "mp4" else "jpg"
        val mimeType = if (mediaType == MediaType.VIDEO) "video/mp4" else "image/jpeg"
        val uniqueFileName = "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$ext"

        val bunnyResult = BunnyStorageHelper.uploadFile(
            context = context,
            fileUri = fileUri,
            folder = "posts",
            fileName = uniqueFileName,
            mimeType = mimeType,
            onProgress = onProgress
        )

        if (bunnyResult.isFailure) {
            return Result.failure(bunnyResult.exceptionOrNull() ?: Exception("Bunny upload failed"))
        }

        val publicCdnUrl = bunnyResult.getOrThrow()
        val postId = "post-${UUID.randomUUID().toString().take(8)}"

        val post = Post(
            id = postId,
            title = title,
            description = description,
            place = place,
            mediaType = mediaType,
            mediaUrl = publicCdnUrl,
            thumbnailUrl = publicCdnUrl,
            reporterId = reporter.id,
            reporterName = reporter.name,
            reporterPhotoUrl = reporter.photoUrl,
            status = PostStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )

        _posts.value = listOf(post) + _posts.value

        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("id", post.id)
                    put("title", post.title)
                    put("description", post.description)
                    put("place", post.place)
                    put("mediaType", post.mediaType.name)
                    put("mediaUrl", post.mediaUrl)
                    put("thumbnailUrl", post.thumbnailUrl)
                    put("reporterId", post.reporterId)
                    put("reporterName", post.reporterName)
                    put("reporterPhotoUrl", post.reporterPhotoUrl)
                    put("status", "PENDING")
                    put("likesCount", 0)
                    put("viewsCount", 0)
                    put("timestamp", post.timestamp)
                }

                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/posts/${post.id}.json")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving post to RTDB: ${e.message}")
            }
        }

        return Result.success(post)
    }

    suspend fun createReporterWithBunny(
        context: Context,
        name: String,
        mobile: String,
        address: String,
        photoUri: Uri?
    ): Reporter {
        val randomNum = (1000..9999).random()
        val generatedId = "REP-$randomNum"
        val generatedPass = "Drikq#" + (100000..999999).random()

        var publicPhotoUrl = ""

        if (photoUri != null) {
            val fileName = "profile_${generatedId}_${System.currentTimeMillis()}.jpg"
            val result = BunnyStorageHelper.uploadFile(
                context = context,
                fileUri = photoUri,
                folder = "profiles",
                fileName = fileName,
                mimeType = "image/jpeg",
                onProgress = {}
            )
            publicPhotoUrl = result.getOrDefault("")
        }

        val reporter = Reporter(
            id = generatedId,
            name = name,
            mobile = mobile,
            address = address,
            photoUrl = publicPhotoUrl,
            password = generatedPass,
            followersCount = 0,
            followingCount = 0
        )

        _reporters.value = _reporters.value + reporter

        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("id", reporter.id)
                    put("name", reporter.name)
                    put("mobile", reporter.mobile)
                    put("address", reporter.address)
                    put("photoUrl", reporter.photoUrl)
                    put("password", reporter.password)
                    put("followersCount", 0)
                    put("followingCount", 0)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/reporters/${reporter.id}.json")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving reporter to RTDB: ${e.message}")
            }
        }

        return reporter
    }

    fun approvePost(postId: String) {
        val list = _posts.value.toMutableList()
        val index = list.indexOfFirst { it.id == postId }
        if (index != -1) {
            val current = list[index]
            val updated = current.copy(status = PostStatus.APPROVED)
            list[index] = updated
            _posts.value = list

            scope.launch {
                try {
                    val body = "\"APPROVED\"".toRequestBody("application/json".toMediaTypeOrNull())
                    val request = Request.Builder()
                        .url("$RTDB_BASE_URL/posts/$postId/status.json")
                        .put(body)
                        .build()
                    httpClient.newCall(request).execute().close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error approving post in RTDB: ${e.message}")
                }
            }
        }
    }

    fun deletePost(postId: String) {
        val postToDelete = _posts.value.find { it.id == postId }
        _posts.value = _posts.value.filter { it.id != postId }

        scope.launch {
            try {
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/posts/$postId.json")
                    .delete()
                    .build()
                httpClient.newCall(request).execute().close()

                if (postToDelete != null && postToDelete.mediaUrl.isNotBlank()) {
                    BunnyStorageHelper.deleteFile(postToDelete.mediaUrl)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting post: ${e.message}")
            }
        }
    }

    fun deleteReporter(reporterId: String) {
        val rep = _reporters.value.find { it.id == reporterId }
        _reporters.value = _reporters.value.filter { it.id != reporterId }

        scope.launch {
            try {
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/reporters/$reporterId.json")
                    .delete()
                    .build()
                httpClient.newCall(request).execute().close()

                if (rep != null && rep.photoUrl.isNotBlank() && rep.photoUrl.contains(BunnyStorageHelper.PUBLIC_CDN_BASE_URL)) {
                    BunnyStorageHelper.deleteFile(rep.photoUrl)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting reporter: ${e.message}")
            }
        }
    }

    /**
     * Requirement 2: Likes persistence:
     * - Save like in Firebase: posts/{postId}/likes/{userId} = true (or DELETE)
     * - Update posts/{postId}/likesCount
     * - After app restart, liked posts still show filled heart
     */
    fun toggleLike(postId: String, userId: String) {
        val list = _posts.value.toMutableList()
        val index = list.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = list[index]
            val liked = post.likedByUsers.contains(userId)
            val updatedLikes = if (liked) {
                post.likedByUsers.remove(userId)
                (post.likesCount - 1).coerceAtLeast(0)
            } else {
                post.likedByUsers.add(userId)
                post.likesCount + 1
            }
            list[index] = post.copy(likesCount = updatedLikes)
            _posts.value = list

            // Update SharedPreferences
            sharedPrefs?.let { prefs ->
                val key = "liked_posts_$userId"
                val set = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
                if (liked) set.remove(postId) else set.add(postId)
                prefs.edit().putStringSet(key, set).apply()
            }

            // Sync with Firebase RTDB
            scope.launch {
                try {
                    // Update posts/{postId}/likes/{userId} = true or delete
                    if (!liked) {
                        val body = "true".toRequestBody("application/json".toMediaTypeOrNull())
                        val req = Request.Builder()
                            .url("$RTDB_BASE_URL/posts/$postId/likes/$userId.json")
                            .put(body)
                            .build()
                        httpClient.newCall(req).execute().close()
                    } else {
                        val req = Request.Builder()
                            .url("$RTDB_BASE_URL/posts/$postId/likes/$userId.json")
                            .delete()
                            .build()
                        httpClient.newCall(req).execute().close()
                    }

                    // Update posts/{postId}/likesCount
                    val countBody = "$updatedLikes".toRequestBody("application/json".toMediaTypeOrNull())
                    val countReq = Request.Builder()
                        .url("$RTDB_BASE_URL/posts/$postId/likesCount.json")
                        .put(countBody)
                        .build()
                    httpClient.newCall(countReq).execute().close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating likes in RTDB: ${e.message}")
                }
            }
        }
    }

    /**
     * Requirement 3: Follow persistence:
     * - Save follow in Firebase: follows/{userId}/{reporterId} = true (or DELETE)
     * - Update reporters/{reporterId}/followersCount
     * - After app restart, show "Following" if already followed
     */
    fun toggleFollowReporter(reporterId: String, userId: String) {
        val list = _reporters.value.toMutableList()
        val index = list.indexOfFirst { it.id == reporterId }
        if (index != -1) {
            val rep = list[index]
            val following = rep.followedByUsers.contains(userId)
            val updatedFollowers = if (following) {
                rep.followedByUsers.remove(userId)
                (rep.followersCount - 1).coerceAtLeast(0)
            } else {
                rep.followedByUsers.add(userId)
                rep.followersCount + 1
            }
            list[index] = rep.copy(followersCount = updatedFollowers)
            _reporters.value = list

            // Update SharedPreferences
            sharedPrefs?.let { prefs ->
                val key = "followed_reps_$userId"
                val set = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
                if (following) set.remove(reporterId) else set.add(reporterId)
                prefs.edit().putStringSet(key, set).apply()
            }

            // Sync with Firebase RTDB
            scope.launch {
                try {
                    if (!following) {
                        val body = "true".toRequestBody("application/json".toMediaTypeOrNull())
                        val req = Request.Builder()
                            .url("$RTDB_BASE_URL/follows/$userId/$reporterId.json")
                            .put(body)
                            .build()
                        httpClient.newCall(req).execute().close()
                    } else {
                        val req = Request.Builder()
                            .url("$RTDB_BASE_URL/follows/$userId/$reporterId.json")
                            .delete()
                            .build()
                        httpClient.newCall(req).execute().close()
                    }

                    val countBody = "$updatedFollowers".toRequestBody("application/json".toMediaTypeOrNull())
                    val countReq = Request.Builder()
                        .url("$RTDB_BASE_URL/reporters/$reporterId/followersCount.json")
                        .put(countBody)
                        .build()
                    httpClient.newCall(countReq).execute().close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating follows in RTDB: ${e.message}")
                }
            }
        }
    }

    /**
     * Requirement 4: Views:
     * - When video starts playing, increase posts/{postId}/viewsCount by 1
     * - Show real viewsCount from Firebase (not 00)
     */
    fun recordView(postId: String) {
        if (viewedPostIds.contains(postId)) return
        viewedPostIds.add(postId)

        val list = _posts.value.toMutableList()
        val index = list.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = list[index]
            val updatedViews = post.viewsCount + 1
            list[index] = post.copy(viewsCount = updatedViews)
            _posts.value = list

            scope.launch {
                try {
                    val body = "$updatedViews".toRequestBody("application/json".toMediaTypeOrNull())
                    val request = Request.Builder()
                        .url("$RTDB_BASE_URL/posts/$postId/viewsCount.json")
                        .put(body)
                        .build()
                    httpClient.newCall(request).execute().close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error recording view in RTDB: ${e.message}")
                }
            }
        }
    }

    fun updatePolicy(newPolicy: String, newTerms: String) {
        _policy.value = AppPolicy(newPolicy, newTerms)
        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("privacyPolicy", newPolicy)
                    put("termsAndConditions", newTerms)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/settings/policy.json")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating policy in RTDB: ${e.message}")
            }
        }
    }

    fun sendPushBroadcast(title: String, body: String, postId: String?) {
        val item = PushNotificationItem(
            id = "notif-${UUID.randomUUID().toString().take(6)}",
            title = title,
            body = body,
            postId = postId,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(item) + _notifications.value

        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("body", item.body)
                    put("postId", item.postId ?: "")
                    put("timestamp", item.timestamp)
                }
                val reqBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/notifications/${item.id}.json")
                    .put(reqBody)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.e(TAG, "Error broadcasting notification: ${e.message}")
            }
        }
    }

    private fun startSettingsRealtimeListener() {
        if (isListeningSettings) return
        isListeningSettings = true
        scope.launch {
            while (true) {
                try {
                    val adsReq = Request.Builder()
                        .url("$RTDB_BASE_URL/settings/adsEnabled.json")
                        .get()
                        .build()
                    httpClient.newCall(adsReq).execute().use { res ->
                        if (res.isSuccessful) {
                            val body = res.body?.string()?.trim()
                            if (!body.isNullOrBlank() && body != "null") {
                                val enabled = body == "true" || body == "\"true\""
                                if (_adsEnabled.value != enabled) {
                                    _adsEnabled.value = enabled
                                    sharedPrefs?.edit()?.putBoolean("ads_enabled", enabled)?.apply()
                                    Log.d(TAG, "Realtime listener updated adsEnabled to: $enabled")
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    suspend fun setAdsEnabled(enabled: Boolean): Boolean {
        _adsEnabled.value = enabled
        sharedPrefs?.edit()?.putBoolean("ads_enabled", enabled)?.apply()
        return try {
            val req = Request.Builder()
                .url("$RTDB_BASE_URL/settings/adsEnabled.json")
                .put(enabled.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            httpClient.newCall(req).execute().use { res ->
                res.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving adsEnabled to RTDB: ${e.message}")
            false
        }
    }
}
