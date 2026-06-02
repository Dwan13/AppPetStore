package com.project.apppetstore.data.model

data class Order(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String? = null,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val total: Double = 0.0,
    val address: String = "",
    val paymentMethod: String = "",
    val status: String = "Confirmado",
    val timestamp: Long = System.currentTimeMillis()
)
