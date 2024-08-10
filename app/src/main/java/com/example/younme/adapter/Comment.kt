package com.example.younme.adapter

data class Comment (
    var uid: String? = null,
    var textComment: String? = null,
    var timestamp: Long? = null,
    var userName: String? = null,
    var userProfileImageUrl: String? = null
)