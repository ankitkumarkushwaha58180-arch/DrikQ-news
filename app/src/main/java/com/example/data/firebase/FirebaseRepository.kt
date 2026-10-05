package com.example.data.firebase

import com.example.data.model.AppPolicy
import com.example.data.model.MediaType
import com.example.data.model.Post
import com.example.data.model.PostStatus
import com.example.data.model.PushNotificationItem
import com.example.data.model.Reporter
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

object FirebaseRepository {
    const val RTDB_BASE_URL = "https://drikq-f9a39-default-rtdb.asia-southeast1.firebasedatabase.app"
    const val PROJECT_ID = "project-825493226391"

    private val httpClient = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current State
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
        seedInitialData()
        fetchFromRemoteRtdb()
    }

    private fun seedInitialData() {
        val rep1 = Reporter(
            id = "REP-1042",
            name = "Ravi Sharma",
            mobile = "+91 98765 43210",
            address = "Central Bureau, New Delhi",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
            password = "RepPass#1042",
            followersCount = 1420,
            followingCount = 18
        )
        val rep2 = Reporter(
            id = "REP-2098",
            name = "Priya Sen",
            mobile = "+91 91234 56789",
            address = "East Zone Desk, Kolkata",
            photoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80",
            password = "RepPass#2098",
            followersCount = 2890,
            followingCount = 35
        )
        val rep3 = Reporter(
            id = "REP-3401",
            name = "Arjun Mehta",
            mobile = "+91 99887 76655",
            address = "Tech & Metro Beat, Bengaluru",
            photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
            password = "RepPass#3401",
            followersCount = 890,
            followingCount = 12
        )

        _reporters.value = listOf(rep1, rep2, rep3)

        val post1 = Post(
            id = "post-101",
            title = "New High-Speed Metro Corridor Inaugurated in City Center",
            description = "The state-of-the-art elevated metro corridor spans 18 kilometers, reducing peak-hour commute times by over 45 minutes. Thousands gathered for the inaugural flag-off ceremony this morning.",
            place = "Downtown Metro Junction",
            mediaType = MediaType.VIDEO,
            mediaUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?auto=format&fit=crop&w=1080&q=80",
            thumbnailUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?auto=format&fit=crop&w=1080&q=80",
            reporterId = rep1.id,
            reporterName = rep1.name,
            reporterPhotoUrl = rep1.photoUrl,
            status = PostStatus.APPROVED,
            likesCount = 342,
            viewsCount = 4120,
            timestamp = System.currentTimeMillis() - 3600000 * 4
        )

        val post2 = Post(
            id = "post-102",
            title = "Historic Heritage Walk Draws Hundreds of International Travelers",
            description = "Old town architecture was illuminated at sunset as local historians guided citizens through ancient stone temples and restored colonial landmarks.",
            place = "Old Heritage Quarter",
            mediaType = MediaType.PHOTO,
            mediaUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=1080&q=80",
            thumbnailUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=1080&q=80",
            reporterId = rep2.id,
            reporterName = rep2.name,
            reporterPhotoUrl = rep2.photoUrl,
            status = PostStatus.APPROVED,
            likesCount = 512,
            viewsCount = 6200,
            timestamp = System.currentTimeMillis() - 3600000 * 12
        )

        val post3 = Post(
            id = "post-103",
            title = "Severe Flash Flood Warning: Relief Camps Mobilized in North Sector",
            description = "Heavy rainfall overnight led to rising river levels. Disaster response teams have deployed emergency boats and distributed drinking water kits to affected families.",
            place = "North River Basin",
            mediaType = MediaType.VIDEO,
            mediaUrl = "https://images.unsplash.com/photo-1547683905-f686c993aae5?auto=format&fit=crop&w=1080&q=80",
            thumbnailUrl = "https://images.unsplash.com/photo-1547683905-f686c993aae5?auto=format&fit=crop&w=1080&q=80",
            reporterId = rep1.id,
            reporterName = rep1.name,
            reporterPhotoUrl = rep1.photoUrl,
            status = PostStatus.APPROVED,
            likesCount = 189,
            viewsCount = 2840,
            timestamp = System.currentTimeMillis() - 3600000 * 24
        )

        val post4 = Post(
            id = "post-104",
            title = "Smart Agriculture Drone Expo Highlights Green Innovations",
            description = "Farmers tested AI-driven spray drones and moisture sensors aimed at reducing water waste by 30% across rural districts.",
            place = "Agri-Tech Pavilion",
            mediaType = MediaType.PHOTO,
            mediaUrl = "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=1080&q=80",
            thumbnailUrl = "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=1080&q=80",
            reporterId = rep3.id,
            reporterName = rep3.name,
            reporterPhotoUrl = rep3.photoUrl,
            status = PostStatus.PENDING,
            likesCount = 0,
            viewsCount = 14,
            timestamp = System.currentTimeMillis() - 3600000 * 2
        )

        _posts.value = listOf(post1, post2, post3, post4)

        _notifications.value = listOf(
            PushNotificationItem(
                id = "notif-1",
                title = "Breaking: High-Speed Metro Corridor Open",
                body = "Commuter trains are officially rolling! Tap to view ground footage.",
                postId = post1.id
            )
        )
    }

    private fun fetchFromRemoteRtdb() {
        scope.launch {
            try {
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
            } catch (e: Exception) {
                // Silently fallback to seeded local cache
            }
        }
    }

    fun loginWithGoogle(email: String, displayName: String, photoUrl: String) {
        val user = UserProfile(
            uid = "usr_" + UUID.randomUUID().toString().take(8),
            name = displayName,
            email = email,
            photoUrl = photoUrl.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80" }
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

    fun authenticateAdmin(pinOrPassword: String): Boolean {
        val clean = pinOrPassword.trim()
        // Admin credentials: accepts admin / admin123 or 1234
        val success = clean == "admin" || clean == "admin123" || clean == "1234"
        if (success) {
            _isAdminLoggedIn.value = true
        }
        return success
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    suspend fun uploadPost(
        title: String,
        description: String,
        place: String,
        mediaType: MediaType,
        mediaUrl: String,
        reporter: Reporter,
        onProgress: (Int) -> Unit
    ): Post {
        // Real-time simulated upload progress 0 to 100%
        for (p in 0..100 step 10) {
            delay(120)
            onProgress(p)
        }

        val newPost = Post(
            id = "post-" + UUID.randomUUID().toString().take(8),
            title = title,
            description = description,
            place = place,
            mediaType = mediaType,
            mediaUrl = mediaUrl.ifBlank {
                if (mediaType == MediaType.VIDEO)
                    "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1080&q=80"
                else
                    "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?auto=format&fit=crop&w=1080&q=80"
            },
            thumbnailUrl = mediaUrl.ifBlank {
                "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?auto=format&fit=crop&w=1080&q=80"
            },
            reporterId = reporter.id,
            reporterName = reporter.name,
            reporterPhotoUrl = reporter.photoUrl,
            status = PostStatus.PENDING, // Uploaded content status should be "pending" by default
            likesCount = 0,
            viewsCount = 1,
            timestamp = System.currentTimeMillis()
        )

        _posts.value = listOf(newPost) + _posts.value

        // Async write to Firebase RTDB
        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("id", newPost.id)
                    put("title", newPost.title)
                    put("description", newPost.description)
                    put("place", newPost.place)
                    put("mediaType", newPost.mediaType.name)
                    put("mediaUrl", newPost.mediaUrl)
                    put("reporterId", newPost.reporterId)
                    put("reporterName", newPost.reporterName)
                    put("status", newPost.status.name)
                    put("timestamp", newPost.timestamp)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/posts/${newPost.id}.json")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (ignored: Exception) {}
        }

        return newPost
    }

    fun approvePost(postId: String) {
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(status = PostStatus.APPROVED) else it
        }
        scope.launch {
            try {
                val body = "\"APPROVED\"".toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/posts/$postId/status.json")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (ignored: Exception) {}
        }
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value.filterNot { it.id == postId }
        scope.launch {
            try {
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/posts/$postId.json")
                    .delete()
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (ignored: Exception) {}
        }
    }

    fun toggleLike(postId: String, userId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val alreadyLiked = post.likedByUsers.contains(userId)
                val newLikedBy = if (alreadyLiked) post.likedByUsers - userId else post.likedByUsers + userId
                val newCount = if (alreadyLiked) maxOf(0, post.likesCount - 1) else post.likesCount + 1
                post.copy(likesCount = newCount, likedByUsers = newLikedBy)
            } else post
        }
    }

    fun incrementView(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) post.copy(viewsCount = post.viewsCount + 1) else post
        }
    }

    fun createReporter(name: String, mobile: String, address: String, photoUrl: String): Reporter {
        val randomNum = (1000..9999).random()
        val generatedId = "REP-$randomNum"
        val generatedPassword = "Drikq#" + (100000..999999).random()

        val newReporter = Reporter(
            id = generatedId,
            name = name,
            mobile = mobile,
            address = address,
            photoUrl = photoUrl.ifBlank {
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"
            },
            password = generatedPassword,
            followersCount = 0,
            followingCount = 0
        )

        _reporters.value = _reporters.value + newReporter

        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("id", newReporter.id)
                    put("name", newReporter.name)
                    put("mobile", newReporter.mobile)
                    put("address", newReporter.address)
                    put("photoUrl", newReporter.photoUrl)
                    put("password", newReporter.password)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/reporters/${newReporter.id}.json")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (ignored: Exception) {}
        }

        return newReporter
    }

    fun updateReporter(reporter: Reporter) {
        _reporters.value = _reporters.value.map { if (it.id == reporter.id) reporter else it }
    }

    fun deleteReporter(reporterId: String) {
        _reporters.value = _reporters.value.filterNot { it.id == reporterId }
        scope.launch {
            try {
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/reporters/$reporterId.json")
                    .delete()
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (ignored: Exception) {}
        }
    }

    fun toggleFollowReporter(reporterId: String, currentUserId: String) {
        _reporters.value = _reporters.value.map { rep ->
            if (rep.id == reporterId) {
                val isFollowed = rep.followedByUsers.contains(currentUserId)
                val newFollowers = if (isFollowed) rep.followedByUsers - currentUserId else rep.followedByUsers + currentUserId
                val newCount = if (isFollowed) maxOf(0, rep.followersCount - 1) else rep.followersCount + 1
                rep.copy(followersCount = newCount, followedByUsers = newFollowers)
            } else rep
        }
    }

    fun updatePrivacyPolicy(policyText: String, termsText: String) {
        _policy.value = AppPolicy(privacyPolicy = policyText, termsAndConditions = termsText)
        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("privacyPolicy", policyText)
                    put("termsAndConditions", termsText)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/settings/policy.json")
                    .put(body)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (ignored: Exception) {}
        }
    }

    fun sendPushNotification(title: String, body: String, postId: String?): PushNotificationItem {
        val item = PushNotificationItem(
            id = "notif-" + UUID.randomUUID().toString().take(6),
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
                val reqBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$RTDB_BASE_URL/notifications/${item.id}.json")
                    .put(reqBody)
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (ignored: Exception) {}
        }

        return item
    }
}
