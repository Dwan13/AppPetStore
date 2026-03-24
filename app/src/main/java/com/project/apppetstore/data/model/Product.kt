package com.project.apppetstore.data.model

import androidx.annotation.DrawableRes

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: String,
    @DrawableRes val imageRes: Int
)

