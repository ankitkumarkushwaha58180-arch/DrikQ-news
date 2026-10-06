package com.example.data.model

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String = ""
)

data class AppPolicy(
    val privacyPolicy: String = "",
    val termsAndConditions: String = ""
)

data class PushNotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val postId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
