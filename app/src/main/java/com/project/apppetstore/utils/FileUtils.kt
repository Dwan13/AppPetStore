package com.project.apppetstore.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.core.content.FileProvider
import java.io.File

private const val FILE_UTILS_TAG = "FileUtils"

fun createTempImageFileInInternalPicturesFolder(name:String, context: Context): Uri {
    val tmpFile = File.createTempFile(
        name,
        ".jpg",
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tmpFile
    )
}

fun createImageOnPhotosFolder(name:String, context: Context): ImageCapture.OutputFileOptions {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    return ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()
}

fun createVideoOnMoviesFolder(name: String, context: Context): Uri? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "$name.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/AppPetStore")
            }

            context.contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        } else {
            val movieDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                ?: context.filesDir
            val tmpFile = File.createTempFile(name, ".mp4", movieDir)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tmpFile
            )
        }
    } catch (exception: Exception) {
        Log.e(FILE_UTILS_TAG, "Error creating video Uri", exception)
        null
    }
}
