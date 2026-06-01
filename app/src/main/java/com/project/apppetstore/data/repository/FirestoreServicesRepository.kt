package com.project.apppetstore.data.repository

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.apppetstore.R
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.data.util.drawableToJpegBytes
import com.project.apppetstore.data.util.uploadImageToStorage
import kotlinx.coroutines.tasks.await

/**
 * Carga servicios desde Firestore (colección "services").
 *
 * Primera ejecución (colección vacía):
 *  - Sube img_cuidador a Firebase Storage en "catalog/services/img_cuidador.jpg"
 *  - Cada servicio guarda ese mismo imageUrl (comparten la misma imagen genérica)
 *
 * Ejecuciones posteriores:
 *  - Lee los documentos de Firestore con imageUrl ya guardado
 *
 * Fallback sin red: datos del mock local.
 */
object FirestoreServicesRepository {

    private val db         get() = Firebase.firestore
    private const val COL = "services"

    suspend fun getServices(context: Context? = null): List<Service> = try {
        val snap = db.collection(COL).get().await()
        when {
            snap.isEmpty -> seedAndReturn(context)
            else         -> snap.documents.mapNotNull { it.toService() }.ifEmpty { seedAndReturn(context) }
        }
    } catch (_: Exception) {
        MockPetShopRepository.getServices()
    }

    // ── Siembra con upload a Storage ──────────────────────────────────────────

    private suspend fun seedAndReturn(context: Context?): List<Service> {
        val services = MockPetShopRepository.getServices()
        return try {
            // Todos los servicios usan img_cuidador → subir una sola vez
            val sharedUrl = if (context != null) {
                val bytes = context.drawableToJpegBytes(R.drawable.img_cuidador)
                uploadImageToStorage("catalog/services/img_cuidador.jpg", bytes)
            } else null

            val batch = db.batch()
            val seeded = services.map { s ->
                val sWithUrl = s.copy(imageUrl = sharedUrl ?: s.imageUrl)
                batch.set(db.collection(COL).document(s.id), sWithUrl.toFirestoreMap())
                sWithUrl
            }
            batch.commit().await()
            seeded
        } catch (_: Exception) {
            services
        }
    }
}

// ── Extensiones privadas ──────────────────────────────────────────────────────

private fun DocumentSnapshot.toService(): Service? = try {
    val name     = getString("name")     ?: return null
    val category = getString("category") ?: return null
    Service(
        id               = getString("id") ?: id,
        name             = name,
        category         = category,
        description      = getString("description") ?: "",
        rating           = getDouble("rating") ?: 0.0,
        distanceKm       = 0.0,
        imageRes         = R.drawable.img_cuidador,
        imageUrl         = getString("imageUrl"),
        lat              = getDouble("lat")  ?: 0.0,
        lng              = getDouble("lng")  ?: 0.0,
        supportsDelivery = getBoolean("supportsDelivery") ?: false
    )
} catch (_: Exception) { null }

private fun Service.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id"               to id,
    "name"             to name,
    "category"         to category,
    "description"      to description,
    "rating"           to rating,
    "lat"              to lat,
    "lng"              to lng,
    "supportsDelivery" to supportsDelivery,
    "imageUrl"         to imageUrl
)
