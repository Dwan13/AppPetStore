package com.project.apppetstore.ui.feature.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.apppetstore.data.repository.SettingsRepository
import kotlinx.coroutines.launch

data class SettingsUiState(
    val searchRadiusKm: Int = 5,
    val notificationsEnabled: Boolean = true,
    /** null = seguir sistema, false = claro, true = oscuro */
    val darkTheme: Boolean? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository.getInstance(app)

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            repo.searchRadius.collect { uiState = uiState.copy(searchRadiusKm = it) }
        }
        viewModelScope.launch {
            repo.darkTheme.collect { uiState = uiState.copy(darkTheme = it) }
        }
        viewModelScope.launch {
            repo.notificationsEnabled.collect { uiState = uiState.copy(notificationsEnabled = it) }
        }
    }

    fun setSearchRadius(km: Int) {
        repo.setSearchRadius(km)
    }

    fun setDarkTheme(dark: Boolean?) {
        repo.setDarkTheme(dark)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        repo.setNotificationsEnabled(enabled)
    }
}
