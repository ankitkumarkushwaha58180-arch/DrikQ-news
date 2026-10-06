package com.example.data.model

data class Post(
    val id: String,
    val title: String,
    val description: String,
    val place: String,
    val mediaType: MediaType,
    val mediaUrl: String, // Bunny.net CDN URL
    val thumbnailUrl: String = mediaUrl,
    val reporterId: String,
    val reporterName: String,
    val reporterPhotoUrl: String = "",
    val status: PostStatus = PostStatus.PENDING,
    var likesCount: Int = 0,
    var viewsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val likedByUsers: MutableList<String> = mutableListOf()
)
