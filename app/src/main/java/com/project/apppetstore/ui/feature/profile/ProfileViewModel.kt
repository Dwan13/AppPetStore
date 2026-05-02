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

    fun register(fullName: String, email: String) {
        login(fullName, email)
    }

    fun updateProfilePhoto(photoUri: String) {
        val currentUser = uiState.user ?: return
        uiState = uiState.copy(
            user = currentUser.copy(profilePhotoUri = photoUri)
        )
    }

    fun updatePetPhoto(photoUri: String) {
        val currentUser = uiState.user ?: return
        uiState = uiState.copy(
            user = currentUser.copy(
                petProfile = currentUser.petProfile.copy(photoUri = photoUri)
            )
        )
    }

    fun updatePetCharacteristics(
        name: String,
        species: String,
        age: String,
        traits: List<String>
    ) {
        val currentUser = uiState.user ?: return
        uiState = uiState.copy(
            user = currentUser.copy(
                petProfile = currentUser.petProfile.copy(
                    name = name,
                    species = species,
                    age = age,
                    traits = traits
                )
            )
        )
    }

    fun logout() {
        uiState = ProfileUiState()
    }
}
