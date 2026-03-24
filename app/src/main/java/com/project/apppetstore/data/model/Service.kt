package com.project.apppetstore.data.model

import androidx.annotation.DrawableRes

data class Service(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val rating: Double, // Nuevo campo
    val distanceKm: Double, // Nuevo campo
    @DrawableRes val imageRes: Int
)
