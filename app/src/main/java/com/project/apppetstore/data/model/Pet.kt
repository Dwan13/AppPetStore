package com.project.apppetstore.data.model

import androidx.annotation.DrawableRes

data class Pet(
    val id: String,
    val name: String,
    val age: String,
    val breed: String,
    val gender: String,
    val size: String,
    val health: String,
    val vaccines: String,
    val personality: String,
    val requirements: String,
    val imageUrl: String? = null,
    @DrawableRes val imageRes: Int? = null
)
