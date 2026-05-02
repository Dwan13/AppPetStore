package com.project.apppetstore.data.model

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO
}

data class ChatAttachment(
    val type: AttachmentType,
    val uri: String
)

data class ChatMessage(
    val id: String,
    val message: String,
    val isUser: Boolean,
    val attachment: ChatAttachment? = null
)
