package com.project.apppetstore.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

/**
 * Convierte un drawable local a bytes JPEG.
 * Funciona con BitmapDrawable y cualquier VectorDrawable/Drawable.
 */
fun Context.drawableToJpegBytes(@DrawableRes resId: Int, quality: Int = 88): ByteArray {
    val drawable = ContextCompat.getDrawable(this, resId) ?: return ByteArray(0)
    val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
        drawable.bitmap
    } else {
        val w = drawable.intrinsicWidth.coerceAtLeast(256)
        val h = drawable.intrinsicHeight.coerceAtLeast(256)
        Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }
    return ByteArrayOutputStream().also { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }.toByteArray()
}

/**
 * Sube bytes a Firebase Storage en [path].
 * Si el archivo ya existe, devuelve la URL sin re-subirlo.
 * Retorna null si falla la subida.
 */
suspend fun uploadImageToStorage(path: String, bytes: ByteArray): String? {
    if (bytes.isEmpty()) return null
    val ref = Firebase.storage.reference.child(path)
    return try {
        // Si ya existe, obtenemos la URL directamente
        ref.downloadUrl.await().toString()
    } catch (_: Exception) {
        try {
            val meta = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
            ref.putBytes(bytes, meta).await()
            ref.downloadUrl.await().toString()
        } catch (_: Exception) {
            null   // sin red o sin permisos → se usará el drawable local
        }
    }
}
