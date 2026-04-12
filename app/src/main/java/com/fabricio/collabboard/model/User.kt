package com.fabricio.collabboard.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val university: String = "",
    val skills: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

