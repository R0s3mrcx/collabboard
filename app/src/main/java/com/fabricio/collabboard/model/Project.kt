package com.fabricio.collabboard.model

data class Project(
    val projectId: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val title: String = "",
    val description: String = "",
    val techStack: String = "",
    val status: String = "open",
    val createdAt: Long = System.currentTimeMillis()
)