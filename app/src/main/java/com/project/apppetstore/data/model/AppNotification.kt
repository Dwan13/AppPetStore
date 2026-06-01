package com.project.apppetstore.data.model

data class AppNotification(
    val id        : String = "",
    val title     : String = "",
    val message   : String = "",
    /** "promo" | "order" | "appointment" */
    val type      : String = "promo",
    val timestamp : Long   = System.currentTimeMillis()
)
