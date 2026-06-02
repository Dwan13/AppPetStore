package com.project.apppetstore.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton que persiste las preferencias de usuario usando SharedPreferences
 * y expone StateFlows para que los ViewModels reaccionen a los cambios.
 */
class SettingsRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // ── Radio de búsqueda (km) ────────────────────────────────────────────────
    private val _searchRadius = MutableStateFlow(prefs.getInt("search_radius_km", 5))
    val searchRadius: StateFlow<Int> = _searchRadius.asStateFlow()

    fun setSearchRadius(km: Int) {
        prefs.edit().putInt("search_radius_km", km).apply()
        _searchRadius.value = km
    }

    // ── Tema oscuro ───────────────────────────────────────────────────────────
    // null = seguir el sistema, false = siempre claro, true = siempre oscuro
    private val _darkTheme: MutableStateFlow<Boolean?> = MutableStateFlow(
        if (prefs.contains("dark_theme")) prefs.getBoolean("dark_theme", false) else null
    )
    val darkTheme: StateFlow<Boolean?> = _darkTheme.asStateFlow()

    fun setDarkTheme(dark: Boolean?) {
        if (dark == null) prefs.edit().remove("dark_theme").apply()
        else prefs.edit().putBoolean("dark_theme", dark).apply()
        _darkTheme.value = dark
    }

    // ── Notificaciones ────────────────────────────────────────────────────────
    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean("notifications_enabled", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }

    // ── Timestamp de última lectura de notificaciones (para el badge) ─────────
    private val _lastSeenTs = MutableStateFlow(prefs.getLong("notif_last_seen", 0L))
    val lastSeenTimestamp: StateFlow<Long> = _lastSeenTs.asStateFlow()

    fun markNotificationsSeen() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("notif_last_seen", now).apply()
        _lastSeenTs.value = now
    }

    // ── Singleton ─────────────────────────────────────────────────────────────
    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
