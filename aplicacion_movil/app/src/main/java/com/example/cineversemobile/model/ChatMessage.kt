package com.example.cineversemobile.model

data class ChatMessage(
    val sender: String,
    val content: String,
    val timestamp: String? = null,
    val recipient: String? = null,
    val type: MessageType = MessageType.CHAT
)

enum class MessageType {
    CHAT, JOIN, LEAVE
}