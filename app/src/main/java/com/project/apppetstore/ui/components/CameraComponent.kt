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
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.project.apppetstore.utils.createImageOnPhotosFolder
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow

const val TAG = "CameraComponent"

@Composable
fun CameraComponent(
    modifier: Modifier = Modifier,
    onPhotoTaken: (Uri) -> Unit,
    onCaptureError: ((ImageCaptureException) -> Unit)? = null,
    isVideoMode: Boolean = false,
    videoOutputFile: File? = null,
    onVideoRecorded: ((Uri) -> Unit)? = null,
    onVideoError: ((Throwable) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val surfaceRequests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val surfaceRequest by surfaceRequests.collectAsState(initial = null)
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<androidx.camera.video.Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

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

        val recorder = androidx.camera.video.Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        provider.unbindAll()
        if (isVideoMode) {
            provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                videoCapture!!,
            )
        } else {
            provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                imageCapture!!,
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            activeRecording = null
        }
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (isVideoMode) {
                        toggleVideoRecording(
                            context = context,
                            videoCapture = videoCapture,
                            currentRecording = activeRecording,
                            outputFile = videoOutputFile,
                            onRecordingChanged = { activeRecording = it },
                            onVideoRecorded = onVideoRecorded,
                            onVideoError = onVideoError
                        )
                    } else {
                        capturePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onPhotoTaken = onPhotoTaken,
                            onCaptureError = onCaptureError
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = if (isVideoMode && activeRecording != null) Icons.Rounded.Stop else if (isVideoMode) Icons.Rounded.Videocam else Icons.Rounded.Camera,
                    contentDescription = if (isVideoMode && activeRecording != null) "Detener grabacion" else if (isVideoMode) "Grabar video" else "Tomar foto"
                )
            }
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

private fun toggleVideoRecording(
    context: Context,
    videoCapture: VideoCapture<androidx.camera.video.Recorder>?,
    currentRecording: Recording?,
    outputFile: File?,
    onRecordingChanged: (Recording?) -> Unit,
    onVideoRecorded: ((Uri) -> Unit)?,
    onVideoError: ((Throwable) -> Unit)?
) {
    if (currentRecording != null) {
        currentRecording.stop()
        onRecordingChanged(null)
        return
    }

    val capture = videoCapture ?: return
    val file = outputFile ?: return
    val output = FileOutputOptions.Builder(file).build()
    val pendingRecording: PendingRecording = capture.output.prepareRecording(context, output)
    val recording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
        handleVideoEvent(event, file, onRecordingChanged, onVideoRecorded, onVideoError)
    }
    onRecordingChanged(recording)
}

private fun handleVideoEvent(
    event: VideoRecordEvent,
    outputFile: File,
    onRecordingChanged: (Recording?) -> Unit,
    onVideoRecorded: ((Uri) -> Unit)?,
    onVideoError: ((Throwable) -> Unit)?
) {
    when (event) {
        is VideoRecordEvent.Finalize -> {
            onRecordingChanged(null)
            if (event.hasError()) {
                onVideoError?.invoke(IllegalStateException("Video finalize error: ${event.error}"))
            } else {
                onVideoRecorded?.invoke(outputFile.toUri())
            }
        }

        is VideoRecordEvent.Start -> Unit
        is VideoRecordEvent.Pause -> Unit
        is VideoRecordEvent.Resume -> Unit
        is VideoRecordEvent.Status -> Unit
    }
}

