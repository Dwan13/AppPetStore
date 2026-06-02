package com.project.apppetstore.data.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    /** "promo" | "order" | "appointment" | "pet" */
    val type: String = "promo",
    val timestamp: Long = System.currentTimeMillis(),
    /** Presente en notificaciones de adopción: identifica el chat al que navegar. */
    val chatId: String? = null
)
