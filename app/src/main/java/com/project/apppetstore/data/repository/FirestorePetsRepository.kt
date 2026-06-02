package com.project.apppetstore.data.repository

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.apppetstore.R
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.util.drawableToJpegBytes
import com.project.apppetstore.data.util.uploadImageToStorage
import kotlinx.coroutines.tasks.await

/**
 * Carga mascotas desde Firestore.
 *
 * Fuentes combinadas:
 *  1. Colección "pets"             → catálogo administrado (admin panel / Firebase Console)
 *  2. Colección "adoptionListings" → mascotas publicadas por usuarios desde la app
 *
 * Primera ejecución (colección "pets" vacía):
 *  - Sube imágenes a Firebase Storage y siembra desde el mock local.
 *
 * Cache en memoria compartida entre HomeViewModel, AdoptionViewModel y FavoritesViewModel.
 */
object FirestorePetsRepository {

    private val db get() = Firebase.firestore
    private const val COL = "pets"

    /** Colección que los usuarios pueden escribir sin restricciones de admin. */
    const val ADOPTION_COL = "adoptionListings"

    @Volatile
    private var cache: List<Pet>? = null

    suspend fun getPets(context: Context? = null): List<Pet> {
        cache?.let { return it }
        val result = loadFromFirestore(context)
        cache = result
        return result
    }

    fun clearCache() {
        cache = null
    }

    // ── Carga principal ───────────────────────────────────────────────────────

    private suspend fun loadFromFirestore(context: Context?): List<Pet> {
        return try {
            // 1. Catálogo admin
            val petsSnap = db.collection(COL).get().await()
            val adminPets = if (petsSnap.isEmpty) {
                seedAndReturn(context)
            } else {
                petsSnap.documents.mapNotNull { it.toPet() }.ifEmpty { seedAndReturn(context) }
            }

            // 2. Mascotas de usuarios publicadas en adopción
            val listingsSnap = db.collection(ADOPTION_COL).get().await()
            val userPets = listingsSnap.documents.mapNotNull { it.toAdoptionListing() }

            // Combinar: el catálogo admin primero, luego las de usuarios
            // Evitar duplicados por id (no debería haberlos, pero por seguridad)
            val adminIds = adminPets.map { it.id }.toSet()
            adminPets + userPets.filter { it.id !in adminIds }
        } catch (_: Exception) {
            MockPetShopRepository.getPets()
        }
    }

    // ── Siembra con upload a Storage ──────────────────────────────────────────

    private suspend fun seedAndReturn(context: Context?): List<Pet> {
        val pets = MockPetShopRepository.getPets()
        return try {
            val batch = db.batch()
            val seeded = pets.map { pet ->
                val imageUrl = if (context != null && pet.imageRes != null) {
                    val bytes = context.drawableToJpegBytes(pet.imageRes)
                    uploadImageToStorage("catalog/pets/${pet.id}.jpg", bytes)
                } else null

                val petWithUrl = pet.copy(imageUrl = imageUrl ?: pet.imageUrl)
                batch.set(db.collection(COL).document(pet.id), petWithUrl.toFirestoreMap())
                petWithUrl
            }
            batch.commit().await()
            seeded
        } catch (_: Exception) {
            pets
        }
    }
}

// ── Extensiones privadas ──────────────────────────────────────────────────────

/** Convierte un documento de la colección "pets" (admin) en Pet. */
private fun DocumentSnapshot.toPet(): Pet? = try {
    val name = getString("name") ?: return null
    Pet(
        id = getString("id") ?: id,
        name = name,
        age = getString("age") ?: "",
        breed = getString("breed") ?: "",
        gender = getString("gender") ?: "",
        size = getString("size") ?: "",
        health = getString("health") ?: "",
        vaccines = getString("vaccines") ?: "",
        personality = getString("personality") ?: "",
        requirements = getString("requirements") ?: "",
        imageUrl = getString("imageUrl"),
        imageRes = getString("imageKey").toDrawableRes(),
        ownerUid = getString("ownerUid")
    )
} catch (_: Exception) {
    null
}

/** Convierte un documento de la colección "adoptionListings" (usuario) en Pet. */
private fun DocumentSnapshot.toAdoptionListing(): Pet? = try {
    val name = getString("name") ?: return null
    Pet(
        id = id,                             // Firestore doc ID = petId del usuario
        name = name,
        age = getString("age") ?: "",
        breed = getString("breed") ?: "",
        gender = getString("gender") ?: "",
        size = getString("size") ?: "",
        health = getString("health") ?: "",
        vaccines = getString("vaccines") ?: "",
        personality = getString("personality") ?: "",
        requirements = getString("requirements") ?: "",
        imageUrl = getString("imageUrl"),
        imageRes = null,
        ownerUid = getString("ownerUid")
    )
} catch (_: Exception) {
    null
}

private fun Pet.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "age" to age,
    "breed" to breed,
    "gender" to gender,
    "size" to size,
    "health" to health,
    "vaccines" to vaccines,
    "personality" to personality,
    "requirements" to requirements,
    "imageKey" to imageRes.toImageKey(),
    "imageUrl" to imageUrl,
    "ownerUid" to ownerUid
)

private fun String?.toDrawableRes(): Int? = when (this) {
    "img_luna" -> R.drawable.img_luna
    "img_max" -> R.drawable.img_max
    "img_rocky" -> R.drawable.img_rocky
    "img_simba" -> R.drawable.img_simba
    else -> null
}

private fun Int?.toImageKey(): String = when (this) {
    R.drawable.img_luna -> "img_luna"
    R.drawable.img_max -> "img_max"
    R.drawable.img_rocky -> "img_rocky"
    R.drawable.img_simba -> "img_simba"
    else -> ""
}
