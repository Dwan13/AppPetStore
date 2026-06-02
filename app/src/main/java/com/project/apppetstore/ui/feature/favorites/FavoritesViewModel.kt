package com.project.apppetstore.ui.feature.favorites

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    val favoritePetIds: Set<String> = emptySet(),
    val favoritePets: List<Pet> = emptyList(),
    val isLoading: Boolean = true
)

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /** Catálogo completo de mascotas — se carga una vez por sesión. */
    private var allPets: List<Pet> = emptyList()

    /** Listener activo de Firestore para los favoritos del usuario actual. */
    private var listenerReg: ListenerRegistration? = null

    var uiState by mutableStateOf(FavoritesUiState())
        private set

    /**
     * Reacciona a cada cambio de autenticación (login, logout, cambio de cuenta)
     * mientras el ViewModel esté vivo.
     *
     * Firebase invoca esto inmediatamente al añadir el listener con el estado
     * actual — por eso reemplaza al antiguo init{}.
     */
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid

        // Cancelar el listener anterior (sesión anterior o primer arranque sin sesión)
        listenerReg?.remove()
        listenerReg = null

        if (uid != null) {
            // Usuario acaba de iniciar sesión (o la app arrancó con sesión activa)
            uiState = uiState.copy(isLoading = true)
            viewModelScope.launch {
                val ctx = getApplication<Application>()
                allPets = try {
                    FirestorePetsRepository.getPets(ctx)
                } catch (_: Exception) {
                    MockPetShopRepository.getPets()
                }
                // Iniciar nuevo listener para este uid
                listenFavorites(uid)
            }
        } else {
            // Usuario cerró sesión — limpiar estado
            allPets = emptyList()
            uiState = FavoritesUiState(isLoading = false)
        }
    }

    init {
        // addAuthStateListener dispara authStateListener de inmediato con el
        // estado actual de auth → no hace falta código adicional en init.
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        listenerReg?.remove()
    }

    // ── Firestore real-time listener ───────────────────────────────────────────

    private fun listenFavorites(uid: String) {
        listenerReg = firestore.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) return@addSnapshotListener
                @Suppress("UNCHECKED_CAST")
                val ids = (doc?.get("favoritePetIds") as? List<String>)?.toSet() ?: emptySet()
                uiState = FavoritesUiState(
                    favoritePetIds = ids,
                    favoritePets = allPets.filter { it.id in ids },
                    isLoading = false
                )
            }
    }

    // ── Acciones ───────────────────────────────────────────────────────────────

    /**
     * Agrega o quita [petId] de favoritos.
     * Actualización optimista → UI responde al instante,
     * luego se persiste en Firestore con merge (sin pisar otros campos).
     */
    fun toggleFavorite(petId: String) {
        val uid = auth.currentUser?.uid ?: return
        val updated = uiState.favoritePetIds.toMutableSet().apply {
            if (contains(petId)) remove(petId) else add(petId)
        }
        // Optimistic update
        uiState = uiState.copy(
            favoritePetIds = updated,
            favoritePets = allPets.filter { it.id in updated }
        )
        // Persist
        firestore.collection("users").document(uid)
            .set(mapOf("favoritePetIds" to updated.toList()), SetOptions.merge())
    }
}
