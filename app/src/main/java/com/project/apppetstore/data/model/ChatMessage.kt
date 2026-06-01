package com.project.apppetstore.data.model

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO
}

data class ChatAttachment(
    val type: AttachmentType,
    val uri: String          // URI local mientras se sube, URL de Storage después
)

data class ChatMessage(
    val id: String = "",
    val message: String = "",
    val isUser: Boolean = true,
    val attachment: ChatAttachment? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val senderId: String = ""
)
