package com.project.apppetstore.ui.feature.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.project.apppetstore.data.model.UserPet
import com.project.apppetstore.utils.AppNotificationHelper

// ── Resultado de operación Firebase ──────────────────────────────────────────

sealed class PetOperationResult {
    data class Success(val message: String) : PetOperationResult()
    data class Failure(val message: String) : PetOperationResult()
}

data class PetsUiState(
    val pets            : List<UserPet>       = emptyList(),
    val isLoading       : Boolean             = true,
    val isSaving        : Boolean             = false,
    val operationResult : PetOperationResult? = null
)

class PetsViewModel(app: Application) : AndroidViewModel(app) {

    private val auth         = FirebaseAuth.getInstance()
    private val firestore    = FirebaseFirestore.getInstance()
    private val storage      = FirebaseStorage.getInstance()
    private var listenerReg  : ListenerRegistration? = null
    private val authListener : FirebaseAuth.AuthStateListener

    var uiState by mutableStateOf(PetsUiState())
        private set

    init {
        authListener = FirebaseAuth.AuthStateListener { fa ->
            val uid = fa.currentUser?.uid
            if (uid != null) { if (listenerReg == null) listenPets(uid) }
            else {
                listenerReg?.remove(); listenerReg = null
                uiState = PetsUiState(isLoading = false)
            }
        }
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        listenerReg?.remove()
    }

    // ── Limpiar resultado ─────────────────────────────────────────────────────

    fun clearOperationResult() {
        uiState = uiState.copy(operationResult = null)
    }

    // ── Agregar mascota ───────────────────────────────────────────────────────

    fun addPet(
        name     : String,
        species  : String,
        age      : String,
        traits   : List<String>,
        photoUri : Uri? = null
    ) {
        val uid   = auth.currentUser?.uid ?: return
        val petRef = firestore.collection("users").document(uid).collection("pets").document()
        val petId  = petRef.id
        val now    = System.currentTimeMillis()

        // Actualización optimista
        val optimisticPet = UserPet(
            id        = petId,
            name      = name,
            species   = species,
            age       = age,
            traits    = traits,
            photoUri  = null,
            createdAt = now
        )
        uiState = uiState.copy(
            pets            = (uiState.pets + optimisticPet).sortedBy { it.createdAt },
            isSaving        = true,
            operationResult = null
        )

        val baseData = mapOf(
            "name"      to name,
            "species"   to species,
            "age"       to age,
            "traits"    to traits,
            "createdAt" to now
        )

        fun onSaved() {
            uiState = uiState.copy(
                isSaving        = false,
                operationResult = PetOperationResult.Success(
                    "✅ ¡$name fue guardada correctamente en Firebase!"
                )
            )
            AppNotificationHelper.showPetAddedNotification(getApplication(), name)
        }

        fun onError(e: Exception, ctx: String) {
            uiState = uiState.copy(
                pets            = uiState.pets.filter { it.id != petId },
                isSaving        = false,
                operationResult = PetOperationResult.Failure(
                    "❌ No se pudo guardar a $name.\n${e.message ?: "Error desconocido"}"
                )
            )
            Log.e("PetsVM", ctx, e)
        }

        if (photoUri != null) {
            val ref = storage.reference.child("users/$uid/pets/$petId.jpg")
            ref.putFile(photoUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw (task.exception ?: Exception("Upload failed"))
                    ref.downloadUrl
                }
                .addOnSuccessListener { url ->
                    val dataWithPhoto = baseData + mapOf("photoUri" to url.toString())
                    uiState = uiState.copy(
                        pets = uiState.pets.map { p ->
                            if (p.id == petId) p.copy(photoUri = url.toString()) else p
                        }
                    )
                    petRef.set(dataWithPhoto)
                        .addOnSuccessListener { onSaved() }
                        .addOnFailureListener { e -> onError(e, "Error guardando con foto") }
                }
                .addOnFailureListener { e ->
                    Log.w("PetsVM", "Foto falló, guardando sin imagen", e)
                    petRef.set(baseData)
                        .addOnSuccessListener { onSaved() }
                        .addOnFailureListener { err -> onError(err, "Error guardando sin foto") }
                }
        } else {
            petRef.set(baseData)
                .addOnSuccessListener { onSaved() }
                .addOnFailureListener { e -> onError(e, "Error guardando mascota") }
        }
    }

    // ── Editar mascota ────────────────────────────────────────────────────────

    fun updatePet(
        petId    : String,
        name     : String,
        species  : String,
        age      : String,
        traits   : List<String>,
        photoUri : Uri? = null
    ) {
        val uid         = auth.currentUser?.uid ?: return
        val petRef      = firestore.collection("users").document(uid).collection("pets").document(petId)
        val previousPet = uiState.pets.find { it.id == petId }

        uiState = uiState.copy(
            pets = uiState.pets.map { p ->
                if (p.id == petId) p.copy(name = name, species = species, age = age, traits = traits)
                else p
            },
            isSaving        = true,
            operationResult = null
        )

        val baseData = mapOf<String, Any>(
            "name"    to name,
            "species" to species,
            "age"     to age,
            "traits"  to traits
        )

        fun onDone() {
            uiState = uiState.copy(
                isSaving        = false,
                operationResult = PetOperationResult.Success(
                    "✅ Los datos de $name fueron actualizados correctamente."
                )
            )
        }
        fun onErr(e: Exception, ctx: String) {
            previousPet?.let { prev ->
                uiState = uiState.copy(
                    pets            = uiState.pets.map { p -> if (p.id == petId) prev else p },
                    isSaving        = false,
                    operationResult = PetOperationResult.Failure(
                        "❌ No se pudo actualizar a $name.\n${e.message ?: "Error desconocido"}"
                    )
                )
            } ?: run {
                uiState = uiState.copy(
                    isSaving        = false,
                    operationResult = PetOperationResult.Failure(
                        "❌ No se pudo actualizar a $name.\n${e.message ?: "Error desconocido"}"
                    )
                )
            }
            Log.e("PetsVM", ctx, e)
        }

        if (photoUri != null) {
            val ref = storage.reference.child("users/$uid/pets/$petId.jpg")
            ref.putFile(photoUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw (task.exception ?: Exception("Upload failed"))
                    ref.downloadUrl
                }
                .addOnSuccessListener { url ->
                    uiState = uiState.copy(
                        pets = uiState.pets.map { p ->
                            if (p.id == petId) p.copy(photoUri = url.toString()) else p
                        }
                    )
                    petRef.update(baseData + mapOf("photoUri" to url.toString()))
                        .addOnSuccessListener { onDone() }
                        .addOnFailureListener { e -> onErr(e, "Error actualizando con foto") }
                }
                .addOnFailureListener { e ->
                    Log.w("PetsVM", "Foto nueva falló", e)
                    petRef.update(baseData)
                        .addOnSuccessListener { onDone() }
                        .addOnFailureListener { err -> onErr(err, "Error actualizando") }
                }
        } else {
            petRef.update(baseData)
                .addOnSuccessListener { onDone() }
                .addOnFailureListener { e -> onErr(e, "Error actualizando mascota") }
        }
    }

    // ── Eliminar mascota ──────────────────────────────────────────────────────

    fun deletePet(petId: String) {
        val uid        = auth.currentUser?.uid ?: return
        val removedPet = uiState.pets.find { it.id == petId } ?: return
        val petName    = removedPet.name

        uiState = uiState.copy(
            pets            = uiState.pets.filter { it.id != petId },
            operationResult = null
        )

        firestore.collection("users").document(uid)
            .collection("pets").document(petId)
            .delete()
            .addOnSuccessListener {
                AppNotificationHelper.showPetDeletedNotification(getApplication(), petName)
                uiState = uiState.copy(
                    operationResult = PetOperationResult.Success(
                        "✅ $petName fue eliminada de tu perfil."
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e("PetsVM", "Error eliminando mascota", e)
                uiState = uiState.copy(
                    pets            = (uiState.pets + removedPet).sortedBy { it.createdAt },
                    operationResult = PetOperationResult.Failure(
                        "❌ No se pudo eliminar a $petName.\n${e.message ?: "Error desconocido"}"
                    )
                )
            }
    }

    // ── Listener Firestore ────────────────────────────────────────────────────

    private fun listenPets(uid: String) {
        listenerReg = firestore
            .collection("users").document(uid)
            .collection("pets")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("PetsVM", "Snapshot error: ${error.message}", error)
                    uiState = uiState.copy(isLoading = false)
                    return@addSnapshotListener
                }
                val serverPets = snap?.documents
                    ?.mapNotNull { doc ->
                        runCatching {
                            UserPet(
                                id        = doc.id,
                                name      = doc.getString("name")    ?: "",
                                species   = doc.getString("species") ?: "Perro",
                                age       = doc.getString("age")     ?: "",
                                photoUri  = doc.getString("photoUri"),
                                traits    = (doc.get("traits") as? List<*>)
                                                ?.filterIsInstance<String>()
                                            ?: emptyList(),
                                createdAt = doc.getLong("createdAt") ?: 0L
                            )
                        }.getOrNull()
                    }
                    ?.sortedBy { it.createdAt }
                    ?: emptyList()

                if (!uiState.isSaving) {
                    uiState = uiState.copy(pets = serverPets, isLoading = false)
                } else {
                    uiState = uiState.copy(isLoading = false)
                }
            }
    }
}
