package com.project.apppetstore.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.project.apppetstore.utils.createImageOnPhotosFolder
import kotlinx.coroutines.flow.MutableStateFlow

const val TAG = "CameraComponent"

@Composable
fun CameraComponent(
    modifier: Modifier = Modifier,
    onPhotoTaken: (Uri) -> Unit,
    onCaptureError: ((ImageCaptureException) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val surfaceRequests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val surfaceRequest by surfaceRequests.collectAsState(initial = null)
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    var useFront by rememberSaveable { mutableStateOf(false) }
    val selector = if (useFront) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
        CameraSelector.DEFAULT_BACK_CAMERA
    }

    LaunchedEffect(selector) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { req -> surfaceRequests.value = req }
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            selector,
            preview,
            imageCapture!!,
        )
    }

    Box(modifier) {
        surfaceRequest?.let { req ->
            CameraXViewfinder(
                surfaceRequest = req,
                modifier = Modifier.fillMaxSize()
            )
        }

        FloatingActionButton(
            onClick = { useFront = !useFront },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Cameraswitch,
                contentDescription = "Cambiar camara"
            )
        }

        FloatingActionButton(
            onClick = {
                capturePhoto(
                    context = context,
                    imageCapture = imageCapture,
                    onPhotoTaken = onPhotoTaken,
                    onCaptureError = onCaptureError
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Camera,
                contentDescription = "Tomar foto"
            )
        }
    }
}

@Deprecated("Use CameraComponent")
@Composable
fun CameraComponen(modifier: Modifier = Modifier, onPhotoTaken: (Uri) -> Unit) {
    CameraComponent(modifier = modifier, onPhotoTaken = onPhotoTaken)
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onPhotoTaken: (Uri) -> Unit,
    onCaptureError: ((ImageCaptureException) -> Unit)? = null
) {
    val capture = imageCapture ?: return
    val name = "PHOTOBOOTH_IMG_${System.currentTimeMillis()}.jpg"

    capture.takePicture(
        createImageOnPhotosFolder(name, context),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri
                if (savedUri != null) {
                    onPhotoTaken(savedUri)
                } else {
                    Log.w(TAG, "Photo saved without URI")
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo error", exception)
                onCaptureError?.invoke(exception)
            }
        }
    )
}