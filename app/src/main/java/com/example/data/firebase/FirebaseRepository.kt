package com.example.data.firebase

import android.content.Context
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

    private val httpClient = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO)

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

    init {
        fetchFromRemoteRtdb()
    }

    private fun fetchFromRemoteRtdb() {
        scope.launch {
            try {
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
                                    list.add(
                                        Reporter(
                                            id = obj.optString("id", key),
                                            name = obj.optString("name"),
                                            mobile = obj.optString("mobile"),
                                            address = obj.optString("address"),
                                            photoUrl = obj.optString("photoUrl"),
                                            password = obj.optString("password"),
                                            followersCount = obj.optInt("followersCount", 0),
                                            followingCount = obj.optInt("followingCount", 0)
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
                                    val statusStr = obj.optString("status", "PENDING").uppercase()
                                    val mediaTypeStr = obj.optString("mediaType", "VIDEO").uppercase()
                                    list.add(
                                        Post(
                                            id = obj.optString("id", key),
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
                                            likesCount = obj.optInt("likesCount", 0),
                                            viewsCount = obj.optInt("viewsCount", 0),
                                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
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
        val user = UserProfile(
            uid = "usr_" + UUID.randomUUID().toString().take(8),
            name = displayName,
            email = email,
            photoUrl = photoUrl
        )
        _currentUser.value = user
    }

    fun logoutUser() {
        _currentUser.value = null
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

    fun loginAdmin(passcode: String): Boolean {
        return if (passcode.trim() == "admin" || passcode.trim() == "admin123") {
            _isAdminLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    /**
     * Uploads media to Bunny.net Edge Storage and saves post metadata to Firebase RTDB.
     */
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

        // 1. Upload to Bunny.net
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

        // 2. Save metadata into local state and Firebase RTDB
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

    /**
     * Creates new reporter, uploading profile photo to Bunny.net if a Uri is supplied.
     */
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

    /**
     * Deletes post from local state, RTDB, AND deletes the file from Bunny Storage!
     */
    fun deletePost(postId: String) {
        val postToDelete = _posts.value.find { it.id == postId }
        _posts.value = _posts.value.filter { it.id != postId }

        scope.launch {
            try {
                // Delete from Firebase RTDB
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/posts/$postId.json")
                    .delete()
                    .build()
                httpClient.newCall(request).execute().close()

                // Delete from Bunny Edge Storage
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

                // Also delete avatar if stored on Bunny
                if (rep != null && rep.photoUrl.isNotBlank() && rep.photoUrl.contains(BunnyStorageHelper.PUBLIC_CDN_BASE_URL)) {
                    BunnyStorageHelper.deleteFile(rep.photoUrl)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting reporter: ${e.message}")
            }
        }
    }

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

            scope.launch {
                try {
                    val body = "$updatedLikes".toRequestBody("application/json".toMediaTypeOrNull())
                    val request = Request.Builder()
                        .url("$RTDB_BASE_URL/posts/$postId/likesCount.json")
                        .put(body)
                        .build()
                    httpClient.newCall(request).execute().close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating likes in RTDB: ${e.message}")
                }
            }
        }
    }

    fun recordView(postId: String) {
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
}
