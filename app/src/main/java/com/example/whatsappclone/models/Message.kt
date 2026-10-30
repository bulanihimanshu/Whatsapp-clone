package com.example.whatsappclone.models

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    var id: String = ""
)
