package com.project.apppetstore.ui.feature.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.repository.FirestorePetsRepository
import com.project.apppetstore.data.repository.MockPetShopRepository
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favoritePetIds : Set<String> = emptySet(),
    val favoritePets   : List<Pet>   = emptyList(),
    val isLoading      : Boolean     = true
)

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /** Catálogo completo — se llena desde Firestore (o caché en memoria). */
    private var allPets: List<Pet> = emptyList()

    private var listenerReg: ListenerRegistration? = null

    var uiState by mutableStateOf(FavoritesUiState())
        private set

    init {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            allPets = try {
                FirestorePetsRepository.getPets(ctx)
            } catch (_: Exception) {
                MockPetShopRepository.getPets()
            }
            // Iniciar listener de favoritos una vez que tenemos el catálogo
            listenFavorites()
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerReg?.remove()
    }

    /**
     * Escucha en tiempo real el campo `favoritePetIds` del documento del usuario
     * en Firestore. Cualquier cambio (desde otro dispositivo, o hecho aquí) se
     * refleja automáticamente en la UI.
     */
    private fun listenFavorites() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            uiState = FavoritesUiState(isLoading = false)
            return
        }
        listenerReg = firestore.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                @Suppress("UNCHECKED_CAST")
                val ids = (doc?.get("favoritePetIds") as? List<String>)?.toSet() ?: emptySet()
                uiState = FavoritesUiState(
                    favoritePetIds = ids,
                    favoritePets   = allPets.filter { it.id in ids },
                    isLoading      = false
                )
            }
    }

    /**
     * Agrega o quita una mascota de favoritos.
     * Actualización optimista: la UI responde al instante;
     * luego se persiste en Firestore.
     */
    fun toggleFavorite(petId: String) {
        val uid = auth.currentUser?.uid ?: return
        val updated = uiState.favoritePetIds.toMutableSet().apply {
            if (contains(petId)) remove(petId) else add(petId)
        }

        // Actualización optimista
        uiState = uiState.copy(
            favoritePetIds = updated,
            favoritePets   = allPets.filter { it.id in updated }
        )

        // Persistir en Firestore (merge para no pisar otros campos del usuario)
        firestore.collection("users").document(uid)
            .set(mapOf("favoritePetIds" to updated.toList()), SetOptions.merge())
    }
}
