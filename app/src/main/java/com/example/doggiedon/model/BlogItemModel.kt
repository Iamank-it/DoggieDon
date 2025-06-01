package com.example.doggiedon.model

data class BlogItemModel(
    var blogId: String = "", // Document ID (for likes)
    val heading: String = "",
    val username: String = "",
    val date: String = "",
    val post: String = "",
    var likecount: Int = 0,
    var likedby: MutableList<String> = mutableListOf(),
    var savedby: MutableList<String> = mutableListOf(),
    var isSaved: Boolean = false
)
