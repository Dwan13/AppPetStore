package com.project.apppetstore.data.repository

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.project.apppetstore.R
import com.project.apppetstore.data.model.Service
import kotlinx.coroutines.tasks.await

/**
 * Carga servicios desde Firestore (colección "services").
 *
 * Convención de imágenes en Storage:
 *  - Imagen específica por servicio: catalog/services/{serviceId}.jpg
 *  - Fallback global:             catalog/services/img_cuidador.jpg
 *
 * Ejecuciones posteriores:
 *  - Lee los documentos de Firestore con imageUrl ya guardado
 *
 * Fallback sin red: datos del mock local.
 */
object FirestoreServicesRepository {

    private val db      get() = Firebase.firestore
    private val storage get() = Firebase.storage.reference
    private const val COL = "services"
    private const val STORAGE_BASE_PATH = "catalog/services"
    private const val DEFAULT_IMAGE_NAME = "img_cuidador.jpg"

    suspend fun getServices(context: Context? = null): List<Service> = try {
        val snap = db.collection(COL).get().await()
        when {
            snap.isEmpty -> seedAndReturn(context)
            else -> {
                val services = snap.documents.mapNotNull { it.toService() }
                if (services.isEmpty()) seedAndReturn(context)
                else enrichMissingImageUrls(services)
            }
        }
    } catch (_: Exception) {
        MockPetShopRepository.getServices()
    }

    // ── Siembra con upload a Storage ──────────────────────────────────────────

    private suspend fun seedAndReturn(context: Context?): List<Service> {
        val services = MockPetShopRepository.getServices()
        return try {
            val batch = db.batch()
            val seeded = services.map { s ->
                val sWithUrl = s.copy(imageUrl = resolveServiceImageUrl(s.id, s.imageUrl))
                batch.set(db.collection(COL).document(s.id), sWithUrl.toFirestoreMap())
                sWithUrl
            }
            batch.commit().await()
            seeded
        } catch (_: Exception) {
            services
        }
    }

    private suspend fun enrichMissingImageUrls(services: List<Service>): List<Service> {
        val enriched = services.map { service ->
            val resolved = resolveServiceImageUrl(service.id, service.imageUrl)
            service.copy(imageUrl = resolved)
        }

        // Persistimos solo los que recibieron URL resuelta para evitar recomputar en cada arranque.
        val batch = db.batch()
        var hasChanges = false
        enriched.forEachIndexed { index, service ->
            val previous = services[index].imageUrl
            if (!service.imageUrl.isNullOrBlank() && service.imageUrl != previous) {
                batch.update(db.collection(COL).document(service.id), "imageUrl", service.imageUrl)
                hasChanges = true
            }
        }
        if (hasChanges) runCatching { batch.commit().await() }

        return enriched
    }

    private suspend fun resolveServiceImageUrl(serviceId: String, currentUrl: String?): String? {
        val specificPath = "$STORAGE_BASE_PATH/${serviceId.lowercase()}.jpg"
        val fallbackPath = "$STORAGE_BASE_PATH/$DEFAULT_IMAGE_NAME"

        return getDownloadUrlOrNull(specificPath)
            ?: getDownloadUrlOrNull(fallbackPath)
            ?: currentUrl
    }

    private suspend fun getDownloadUrlOrNull(path: String): String? {
        return runCatching { storage.child(path).downloadUrl.await().toString() }.getOrNull()
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
