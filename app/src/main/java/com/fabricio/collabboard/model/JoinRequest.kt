package com.fabricio.collabboard.model

data class JoinRequest(
    val requestId: String = "",
    val projectId: String = "",
    val applicantId: String = "",
    val applicantName: String = "",
    val message: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)