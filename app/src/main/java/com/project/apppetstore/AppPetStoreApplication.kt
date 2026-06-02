package com.project.apppetstore

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.project.apppetstore.utils.AppNotificationHelper

class AppPetStoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    // ── Crear canales (requerido desde Android 8.0 / API 26) ─────────────────

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                AppNotificationHelper.CHANNEL_AUTH,
                "Autenticación",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Inicio y cierre de sesión" },

            NotificationChannel(
                AppNotificationHelper.CHANNEL_ORDERS,
                "Pedidos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Confirmaciones de pedidos" },

            NotificationChannel(
                AppNotificationHelper.CHANNEL_PETS,
                "Mascotas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Registro y cambios en mascotas" },

            NotificationChannel(
                AppNotificationHelper.CHANNEL_PROMOS,
                "Promociones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Ofertas y descuentos especiales" },

            NotificationChannel(
                AppNotificationHelper.CHANNEL_ADOPTION,
                "Adopción",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Interesados en adoptar tus mascotas" }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }
}
