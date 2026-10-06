package com.example.data.model

data class Reporter(
    val id: String,
    val name: String,
    val mobile: String,
    val address: String,
    val photoUrl: String = "",
    val password: String = "",
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    val followedByUsers: MutableList<String> = mutableListOf()
)
