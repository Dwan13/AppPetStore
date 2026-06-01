package com.project.apppetstore.data.model

import androidx.annotation.DrawableRes

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: String,                    // Ej. "$24.99"
    @DrawableRes val imageRes: Int? = null,
    /** URL remota en Firebase Storage (prioridad sobre imageRes). */
    val imageUrl: String? = null,
    val description: String = "",
    val rating: Double = 4.5,
    val reviewCount: Int = 0,
    val stock: Int = 10,
    val discount: Int = 0,                // 0 = sin descuento, 20 = 20% off
    val tags: List<String> = emptyList()
)
