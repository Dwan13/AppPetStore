package com.project.apppetstore.ui.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.project.apppetstore.data.model.UserProfile

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val user: UserProfile? = null
)

class ProfileViewModel : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun login(fullName: String, email: String) {
        uiState = ProfileUiState(
            isLoggedIn = true,
            user = UserProfile(fullName = fullName, email = email)
        )
    }

    fun logout() {
        uiState = ProfileUiState()
    }
}

