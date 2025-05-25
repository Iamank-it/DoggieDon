package com.example.doggiedon.model

data class Blog(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val authorId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)