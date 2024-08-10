package com.example.younme.adapter

data class Status(
    var textStatus: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long? = null,
    var uid: String? = null,
    var profileImageUrl: String? = null,
    var comments: List<Comment>? = null  //comment phải là dạng list
)
