package com.project.apppetstore.data.repository

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.apppetstore.R
import com.project.apppetstore.data.model.Product
import com.project.apppetstore.data.util.drawableToJpegBytes
import com.project.apppetstore.data.util.uploadImageToStorage
import kotlinx.coroutines.tasks.await

/**
 * Carga productos desde Firestore (colección "products").
 *
 * Primera ejecución (colección vacía):
 *  - Sube cada imagen única a Firebase Storage en "catalog/products/{imageKey}.jpg"
 *  - Guarda el documento con imageUrl apuntando al Storage URL
 *
 * Ejecuciones posteriores:
 *  - Lee los documentos con imageUrl ya guardado
 *
 * Fallback sin red: datos del mock local.
 */
object FirestoreProductsRepository {

    private val db         get() = Firebase.firestore
    private const val COL = "products"

    suspend fun getProducts(context: Context? = null): List<Product> = try {
        val snap = db.collection(COL).get().await()
        when {
            snap.isEmpty -> seedAndReturn(context)
            else         -> snap.documents.mapNotNull { it.toProduct() }.ifEmpty { seedAndReturn(context) }
        }
    } catch (_: Exception) {
        MockPetShopRepository.getProducts()
    }

    // ── Siembra con upload a Storage ──────────────────────────────────────────

    private suspend fun seedAndReturn(context: Context?): List<Product> {
        val products = MockPetShopRepository.getProducts()
        return try {
            // Subir cada drawable único una sola vez (varios productos pueden compartir imagen)
            val urlCache = mutableMapOf<String, String?>()
            val seeded = products.map { p ->
                val imageKey = p.imageRes.toImageKey()
                val imageUrl = if (context != null && p.imageRes != null) {
                    urlCache.getOrPut(imageKey) {
                        val bytes = context.drawableToJpegBytes(p.imageRes)
                        uploadImageToStorage("catalog/products/$imageKey.jpg", bytes)
                    }
                } else null

                p.copy(imageUrl = imageUrl ?: p.imageUrl)
            }

            val batch = db.batch()
            seeded.forEach { p ->
                batch.set(db.collection(COL).document(p.id), p.toFirestoreMap())
            }
            batch.commit().await()
            seeded
        } catch (_: Exception) {
            products
        }
    }
}

// ── Extensiones privadas ──────────────────────────────────────────────────────

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toProduct(): Product? = try {
    val name     = getString("name")     ?: return null
    val category = getString("category") ?: return null
    val price    = getString("price")    ?: return null
    Product(
        id          = getString("id") ?: id,
        name        = name,
        category    = category,
        price       = price,
        imageRes    = getString("imageKey").toDrawableRes(),
        imageUrl    = getString("imageUrl"),
        description = getString("description") ?: "",
        rating      = getDouble("rating") ?: 4.5,
        reviewCount = getLong("reviewCount")?.toInt() ?: 0,
        stock       = getLong("stock")?.toInt() ?: 10,
        discount    = getLong("discount")?.toInt() ?: 0,
        tags        = (get("tags") as? List<String>) ?: emptyList()
    )
} catch (_: Exception) { null }

private fun Product.toFirestoreMap(): Map<String, Any?> = buildMap {
    put("id",          id)
    put("name",        name)
    put("category",    category)
    put("price",       price)
    put("imageKey",    imageRes.toImageKey())
    put("imageUrl",    imageUrl)
    put("description", description)
    put("rating",      rating)
    put("reviewCount", reviewCount)
    put("stock",       stock)
    put("discount",    discount)
    put("tags",        tags)
}

private fun String?.toDrawableRes(): Int? = when (this) {
    "img_comida"   -> R.drawable.img_comida
    "img_juguetes" -> R.drawable.img_juguetes
    "img_gym"      -> R.drawable.img_gym
    "img_correa"   -> R.drawable.img_correa
    else           -> R.drawable.img_comida
}

private fun Int?.toImageKey(): String = when (this) {
    R.drawable.img_comida   -> "img_comida"
    R.drawable.img_juguetes -> "img_juguetes"
    R.drawable.img_gym      -> "img_gym"
    R.drawable.img_correa   -> "img_correa"
    else                    -> "img_comida"
}
