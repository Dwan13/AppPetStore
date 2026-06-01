package com.project.apppetstore.data.model

import androidx.annotation.DrawableRes

data class Service(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val rating: Double,
    val distanceKm: Double,
    @DrawableRes val imageRes: Int,
    /** URL remota en Firebase Storage (prioridad sobre imageRes). */
    val imageUrl: String? = null,
    /** Latitud real del lugar en Bogotá (0.0 = sin coordenada). */
    val lat: Double = 0.0,
    /** Longitud real del lugar en Bogotá (0.0 = sin coordenada). */
    val lng: Double = 0.0,
    /** true = el servicio puede enviarse a domicilio; false = solo cita presencial. */
    val supportsDelivery: Boolean = false
)
