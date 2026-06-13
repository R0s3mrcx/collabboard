package com.fabricio.collabboard.model

import com.google.firebase.Timestamp

data class JoinRequest(
    val requestId: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val applicantId: String = "",
    val applicantName: String = "",
    val message: String = "",
    val status: String = "pending", // "pending" | "accepted" | "rejected"
    val createdAt: Timestamp = Timestamp.now()
)
