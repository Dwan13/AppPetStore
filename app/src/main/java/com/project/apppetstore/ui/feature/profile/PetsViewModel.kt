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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.project.apppetstore.data.model.UserPet
import com.project.apppetstore.data.repository.FirestorePetsRepository
import com.project.apppetstore.utils.AppNotificationHelper

// ── Resultado de operación Firebase ──────────────────────────────────────────

sealed class PetOperationResult {
    data class Success(val message: String) : PetOperationResult()
    data class Failure(val message: String) : PetOperationResult()
}

data class PetsUiState(
    val pets               : List<UserPet>       = emptyList(),
    val isLoading          : Boolean             = true,
    val isSaving           : Boolean             = false,
    val operationResult    : PetOperationResult? = null,
    /** IDs de mascotas cuyo toggle de adopción está en curso (para mostrar loading por tarjeta). */
    val togglingAdoptionIds: Set<String>         = emptySet()
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
        gender   : String,
        size     : String,
        health   : String,
        vaccines : String,
        requirements: String,
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
            gender    = gender,
            size      = size,
            health    = health,
            vaccines  = vaccines,
            requirements = requirements,
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
            "gender"    to gender,
            "size"      to size,
            "health"    to health,
            "vaccines"  to vaccines,
            "requirements" to requirements,
            "traits"    to traits,
            "createdAt" to now
        )

        fun onSaved() {
            uiState = uiState.copy(
                isSaving        = false,
                operationResult = PetOperationResult.Success(
                    "$name fue guardada correctamente."
                )
            )
            AppNotificationHelper.showPetAddedNotification(getApplication(), name)
        }

        fun onError(e: Exception, ctx: String) {
            uiState = uiState.copy(
                pets            = uiState.pets.filter { it.id != petId },
                isSaving        = false,
                operationResult = PetOperationResult.Failure(
                    "No se pudo guardar a $name.\n${e.message ?: "Error desconocido"}"
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
        gender   : String,
        size     : String,
        health   : String,
        vaccines : String,
        requirements: String,
        traits   : List<String>,
        photoUri : Uri? = null
    ) {
        val uid         = auth.currentUser?.uid ?: return
        val petRef      = firestore.collection("users").document(uid).collection("pets").document(petId)
        val previousPet = uiState.pets.find { it.id == petId }
        val shouldSyncAdoptionListing = previousPet?.isAvailableForAdoption == true

        uiState = uiState.copy(
            pets = uiState.pets.map { p ->
                if (p.id == petId) {
                    p.copy(
                        name = name,
                        species = species,
                        age = age,
                        gender = gender,
                        size = size,
                        health = health,
                        vaccines = vaccines,
                        requirements = requirements,
                        traits = traits
                    )
                }
                else p
            },
            isSaving        = true,
            operationResult = null
        )

        val baseData = mapOf<String, Any>(
            "name"    to name,
            "species" to species,
            "age"     to age,
            "gender"  to gender,
            "size"    to size,
            "health"  to health,
            "vaccines" to vaccines,
            "requirements" to requirements,
            "traits"  to traits
        )

        fun onDone(photoUrl: String?) {
            if (shouldSyncAdoptionListing) {
                upsertAdoptionListing(
                    uid = uid,
                    petId = petId,
                    name = name,
                    species = species,
                    age = age,
                    gender = gender,
                    size = size,
                    health = health,
                    vaccines = vaccines,
                    requirements = requirements,
                    traits = traits,
                    photoUri = photoUrl
                )
            }
            uiState = uiState.copy(
                isSaving        = false,
                operationResult = PetOperationResult.Success(
                    "Los datos de $name fueron actualizados correctamente."
                )
            )
        }
        fun onErr(e: Exception, ctx: String) {
            previousPet?.let { prev ->
                uiState = uiState.copy(
                    pets            = uiState.pets.map { p -> if (p.id == petId) prev else p },
                    isSaving        = false,
                    operationResult = PetOperationResult.Failure(
                        "No se pudo actualizar a $name.\n${e.message ?: "Error desconocido"}"
                    )
                )
            } ?: run {
                uiState = uiState.copy(
                    isSaving        = false,
                    operationResult = PetOperationResult.Failure(
                        "No se pudo actualizar a $name.\n${e.message ?: "Error desconocido"}"
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
                        .addOnSuccessListener { onDone(url.toString()) }
                        .addOnFailureListener { e -> onErr(e, "Error actualizando con foto") }
                }
                .addOnFailureListener { e ->
                    Log.w("PetsVM", "Foto nueva falló", e)
                    petRef.update(baseData)
                        .addOnSuccessListener { onDone(previousPet?.photoUri) }
                        .addOnFailureListener { err -> onErr(err, "Error actualizando") }
                }
        } else {
            petRef.update(baseData)
                .addOnSuccessListener { onDone(previousPet?.photoUri) }
                .addOnFailureListener { e -> onErr(e, "Error actualizando mascota") }
        }
    }

    private fun upsertAdoptionListing(
        uid: String,
        petId: String,
        name: String,
        species: String,
        age: String,
        gender: String,
        size: String,
        health: String,
        vaccines: String,
        requirements: String,
        traits: List<String>,
        photoUri: String?
    ) {
        val listingRef = firestore
            .collection(FirestorePetsRepository.ADOPTION_COL)
            .document(petId)

        val data = mutableMapOf<String, Any>(
            "name" to name,
            "breed" to species,
            "age" to age,
            "gender" to gender,
            "size" to size,
            "health" to health,
            "vaccines" to vaccines,
            "personality" to traits.joinToString(", "),
            "requirements" to requirements,
            "ownerUid" to uid
        )
        if (!photoUri.isNullOrBlank()) data["imageUrl"] = photoUri

        listingRef.set(data, SetOptions.merge())
            .addOnSuccessListener { FirestorePetsRepository.clearCache() }
            .addOnFailureListener { e -> Log.e("PetsVM", "Error sincronizando adoptionListing", e) }
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
                        "$petName fue eliminada de tu perfil."
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e("PetsVM", "Error eliminando mascota", e)
                uiState = uiState.copy(
                    pets            = (uiState.pets + removedPet).sortedBy { it.createdAt },
                    operationResult = PetOperationResult.Failure(
                        "No se pudo eliminar a $petName.\n${e.message ?: "Error desconocido"}"
                    )
                )
            }
    }

    // ── Publicar / retirar mascota de adopción ────────────────────────────────

    fun toggleAdoption(petId: String) {
        val uid = auth.currentUser?.uid ?: return
        val pet = uiState.pets.find { it.id == petId } ?: return
        val newValue = !pet.isAvailableForAdoption

        // ── 1. Activar loading para esta tarjeta + actualización optimista ───────
        uiState = uiState.copy(
            togglingAdoptionIds = uiState.togglingAdoptionIds + petId,
            pets = uiState.pets.map { p ->
                if (p.id == petId) p.copy(isAvailableForAdoption = newValue) else p
            }
        )

        // ── 2. Marcar el flag en la colección privada del usuario ─────────────
        // Usamos set+merge en lugar de update() para que funcione aunque el campo
        // no exista todavía en el documento (mascotas creadas antes del feature).
        val userPetRef = firestore
            .collection("users").document(uid)
            .collection("pets").document(petId)
        userPetRef.set(
            mapOf("isAvailableForAdoption" to newValue),
            SetOptions.merge()
        ).addOnFailureListener {
            // Rollback local si el flag no se pudo guardar
            uiState = uiState.copy(
                pets = uiState.pets.map { p ->
                    if (p.id == petId) p.copy(isAvailableForAdoption = !newValue) else p
                }
            )
        }

        // ── 3. Publicar / retirar de "adoptionListings" (colección de usuarios) ──
        // Se usa "adoptionListings" en lugar de "pets" porque la colección "pets"
        // es administrada (admin panel) y los usuarios normales no pueden escribirla.
        val listingRef = firestore
            .collection(FirestorePetsRepository.ADOPTION_COL)
            .document(petId)

        if (newValue) {
            val data = mapOf(
                "name"         to pet.name,
                "breed"        to pet.species,
                "age"          to pet.age,
                "gender"       to pet.gender,
                "size"         to pet.size,
                "health"       to pet.health,
                "vaccines"     to pet.vaccines,
                "personality"  to pet.traits.joinToString(", "),
                "requirements" to pet.requirements,
                "imageUrl"     to pet.photoUri,
                "ownerUid"     to uid
            )
            listingRef.set(data)
                .addOnSuccessListener {
                    FirestorePetsRepository.clearCache()
                    uiState = uiState.copy(
                        togglingAdoptionIds = uiState.togglingAdoptionIds - petId,
                        operationResult     = PetOperationResult.Success(
                            "${pet.name} está publicada en adopción. ¡Otros usuarios la verán pronto!"
                        )
                    )
                }
                .addOnFailureListener { e ->
                    uiState = uiState.copy(
                        togglingAdoptionIds = uiState.togglingAdoptionIds - petId,
                        pets = uiState.pets.map { p ->
                            if (p.id == petId) p.copy(isAvailableForAdoption = false) else p
                        },
                        operationResult = PetOperationResult.Failure(
                            "No se pudo publicar a ${pet.name}: ${e.message}"
                        )
                    )
                    userPetRef.set(mapOf("isAvailableForAdoption" to false), SetOptions.merge())
                }
        } else {
            listingRef.delete()
                .addOnSuccessListener {
                    FirestorePetsRepository.clearCache()
                    uiState = uiState.copy(
                        togglingAdoptionIds = uiState.togglingAdoptionIds - petId,
                        operationResult     = PetOperationResult.Success(
                            "${pet.name} fue retirada del catálogo de adopción."
                        )
                    )
                }
                .addOnFailureListener {
                    uiState = uiState.copy(
                        togglingAdoptionIds = uiState.togglingAdoptionIds - petId
                    )
                }
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
                                id                     = doc.id,
                                name                   = doc.getString("name")    ?: "",
                                species                = doc.getString("species") ?: "Perro",
                                age                    = doc.getString("age")     ?: "",
                                gender                 = doc.getString("gender") ?: "",
                                size                   = doc.getString("size") ?: "",
                                health                 = doc.getString("health") ?: "",
                                vaccines               = doc.getString("vaccines") ?: "",
                                requirements           = doc.getString("requirements") ?: "",
                                photoUri               = doc.getString("photoUri"),
                                traits                 = (doc.get("traits") as? List<*>)
                                                             ?.filterIsInstance<String>()
                                                         ?: emptyList(),
                                createdAt              = doc.getLong("createdAt") ?: 0L,
                                isAvailableForAdoption = doc.getBoolean("isAvailableForAdoption") ?: false
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
