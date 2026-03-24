package com.project.apppetstore.data.model

import androidx.annotation.DrawableRes

data class Service(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    @DrawableRes val imageRes: Int
)

