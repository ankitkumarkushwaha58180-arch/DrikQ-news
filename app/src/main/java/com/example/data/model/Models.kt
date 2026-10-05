package com.example.data.model

enum class MediaType {
    VIDEO,
    PHOTO
}

enum class PostStatus {
    PENDING,
    APPROVED
}

data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val place: String = "",
    val mediaType: MediaType = MediaType.PHOTO,
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reporterPhotoUrl: String = "",
    val status: PostStatus = PostStatus.PENDING,
    val likesCount: Int = 0,
    val viewsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val likedByUsers: List<String> = emptyList()
)

data class Reporter(
    val id: String = "",
    val name: String = "",
    val mobile: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val password: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val followedByUsers: List<String> = emptyList()
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
)

data class PushNotificationItem(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val postId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppPolicy(
    val privacyPolicy: String = "",
    val termsAndConditions: String = ""
)
