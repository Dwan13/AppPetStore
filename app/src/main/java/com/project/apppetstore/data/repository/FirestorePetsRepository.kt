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
 * Carga mascotas desde Firestore (colección "pets").
 *
 * Primera ejecución (colección vacía):
 *  - Sube cada imagen a Firebase Storage en "catalog/pets/{id}.jpg"
 *  - Guarda el documento con imageUrl apuntando al Storage URL
 *
 * Ejecuciones posteriores:
 *  - Lee los documentos de Firestore, que ya contienen imageUrl
 *
 * Fallback sin red: datos del mock local.
 *
 * Cache en memoria: una sola llamada a Firestore por sesión (compartida
 * entre HomeViewModel, AdoptionViewModel y FavoritesViewModel).
 */
object FirestorePetsRepository {

    private val db         get() = Firebase.firestore
    private const val COL = "pets"

    @Volatile private var cache: List<Pet>? = null

    suspend fun getPets(context: Context? = null): List<Pet> {
        cache?.let { return it }
        val result = loadFromFirestore(context)
        cache = result
        return result
    }

    fun clearCache() { cache = null }

    // ── Carga principal ───────────────────────────────────────────────────────

    private suspend fun loadFromFirestore(context: Context?): List<Pet> = try {
        val snap = db.collection(COL).get().await()
        when {
            snap.isEmpty -> seedAndReturn(context)
            else         -> snap.documents.mapNotNull { it.toPet() }.ifEmpty { seedAndReturn(context) }
        }
    } catch (_: Exception) {
        MockPetShopRepository.getPets()
    }

    // ── Siembra con upload a Storage ──────────────────────────────────────────

    private suspend fun seedAndReturn(context: Context?): List<Pet> {
        val pets = MockPetShopRepository.getPets()
        return try {
            val batch = db.batch()
            val seeded = pets.map { pet ->
                // Subir imagen a Firebase Storage si tenemos contexto
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
            pets   // sin red: devolvemos el mock igualmente
        }
    }
}

// ── Extensiones privadas ──────────────────────────────────────────────────────

private fun DocumentSnapshot.toPet(): Pet? = try {
    val name = getString("name") ?: return null
    Pet(
        id           = getString("id")           ?: id,
        name         = name,
        age          = getString("age")          ?: "",
        breed        = getString("breed")        ?: "",
        gender       = getString("gender")       ?: "",
        size         = getString("size")         ?: "",
        health       = getString("health")       ?: "",
        vaccines     = getString("vaccines")     ?: "",
        personality  = getString("personality")  ?: "",
        requirements = getString("requirements") ?: "",
        imageUrl     = getString("imageUrl"),
        imageRes     = getString("imageKey").toDrawableRes()
    )
} catch (_: Exception) { null }

private fun Pet.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id"           to id,
    "name"         to name,
    "age"          to age,
    "breed"        to breed,
    "gender"       to gender,
    "size"         to size,
    "health"       to health,
    "vaccines"     to vaccines,
    "personality"  to personality,
    "requirements" to requirements,
    "imageKey"     to imageRes.toImageKey(),
    "imageUrl"     to imageUrl
)

private fun String?.toDrawableRes(): Int? = when (this) {
    "img_luna"  -> R.drawable.img_luna
    "img_max"   -> R.drawable.img_max
    "img_rocky" -> R.drawable.img_rocky
    "img_simba" -> R.drawable.img_simba
    else        -> null
}

private fun Int?.toImageKey(): String = when (this) {
    R.drawable.img_luna  -> "img_luna"
    R.drawable.img_max   -> "img_max"
    R.drawable.img_rocky -> "img_rocky"
    R.drawable.img_simba -> "img_simba"
    else                 -> ""
}
