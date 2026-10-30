package com.example.whatsappclone.models

data class ChatListItem(
    val userId: String = "",
    val userName: String = "",
    val profileImage: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0
)
