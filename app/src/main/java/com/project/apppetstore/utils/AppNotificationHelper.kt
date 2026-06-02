package com.project.apppetstore.utils

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.project.apppetstore.MainActivity
import com.project.apppetstore.R

/**
 * Helper para mostrar notificaciones locales del sistema Android.
 * Sigue el patrón del guía CL_13:
 *   1. Definir canal en Application.onCreate()
 *   2. Construir la notificación con NotificationCompat.Builder
 *   3. Asociar un PendingIntent para abrir la app
 *   4. Verificar permiso POST_NOTIFICATIONS antes de notificar
 */
object AppNotificationHelper {

    // ── Canales de notificación ───────────────────────────────────────────────
    const val CHANNEL_AUTH     = "channel_auth"
    const val CHANNEL_ORDERS   = "channel_orders"
    const val CHANNEL_PETS     = "channel_pets"
    const val CHANNEL_PROMOS   = "channel_promos"
    const val CHANNEL_ADOPTION = "channel_adoption"

    // ── IDs fijos para eventos únicos ─────────────────────────────────────────
    private const val ID_LOGIN    = 1001
    private const val ID_REGISTER = 1002
    private const val ID_LOGOUT   = 1003

    // ── Inicio de sesión ──────────────────────────────────────────────────────

    fun showLoginNotification(context: Context, userName: String) {
        show(
            context   = context,
            channelId = CHANNEL_AUTH,
            id        = ID_LOGIN,
            title     = "¡Bienvenido, $userName!",
            message   = "Has iniciado sesión correctamente en AppPetStore."
        )
    }

    // ── Registro de cuenta ────────────────────────────────────────────────────

    fun showRegisterNotification(context: Context, userName: String) {
        show(
            context   = context,
            channelId = CHANNEL_AUTH,
            id        = ID_REGISTER,
            title     = "¡Cuenta creada!",
            message   = "Bienvenido a AppPetStore, $userName. ¡Encuentra todo para tu mascota!"
        )
    }

    // ── Cierre de sesión ──────────────────────────────────────────────────────

    fun showLogoutNotification(context: Context) {
        show(
            context   = context,
            channelId = CHANNEL_AUTH,
            id        = ID_LOGOUT,
            title     = "Sesión cerrada",
            message   = "Has cerrado sesión correctamente. ¡Hasta pronto!"
        )
    }

    // ── Pedido confirmado ─────────────────────────────────────────────────────

    fun showOrderNotification(context: Context, productName: String) {
        show(
            context   = context,
            channelId = CHANNEL_ORDERS,
            id        = uniqueId(),
            title     = "✅ Pedido confirmado",
            message   = "Tu pedido de $productName fue recibido y está siendo procesado."
        )
    }

    // ── Mascota añadida ───────────────────────────────────────────────────────

    fun showPetAddedNotification(context: Context, petName: String) {
        show(
            context   = context,
            channelId = CHANNEL_PETS,
            id        = uniqueId(),
            title     = "🐾 Mascota registrada",
            message   = "$petName ha sido añadida a tu perfil correctamente."
        )
    }

    // ── Mascota eliminada ─────────────────────────────────────────────────────

    fun showPetDeletedNotification(context: Context, petName: String) {
        show(
            context   = context,
            channelId = CHANNEL_PETS,
            id        = uniqueId(),
            title     = "Mascota eliminada",
            message   = "$petName ha sido eliminada de tu perfil."
        )
    }

    // ── Interés en adopción ───────────────────────────────────────────────────

    fun showAdoptionInterestNotification(context: Context, petName: String) {
        show(
            context   = context,
            channelId = CHANNEL_ADOPTION,
            id        = uniqueId(),
            title     = "Nuevo interesado en $petName",
            message   = "Alguien quiere adoptar a $petName. Abre el chat para responder."
        )
    }

    // ── Nueva notificación de promoción ───────────────────────────────────────

    fun showPromoNotification(context: Context, title: String, message: String) {
        show(
            context   = context,
            channelId = CHANNEL_PROMOS,
            id        = uniqueId(),
            title     = title,
            message   = message
        )
    }

    // ── Núcleo: construye y muestra la notificación ───────────────────────────

    private fun show(
        context   : Context,
        channelId : String,
        id        : Int,
        title     : String,
        message   : String
    ) {
        // PendingIntent → abre MainActivity al tocar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,                                         // requestCode único por notificación
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Builder del canal correspondiente
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_bell_dot)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as? NotificationManager ?: return

        // Verificar permiso POST_NOTIFICATIONS (requerido desde Android 13 / API 33)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(id, notification)
        }
    }

    /** ID único basado en timestamp para no sobreescribir notificaciones. */
    private fun uniqueId(): Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
}
