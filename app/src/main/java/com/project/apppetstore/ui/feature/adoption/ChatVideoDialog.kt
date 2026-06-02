package com.project.apppetstore.ui.feature.adoption

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.project.apppetstore.ui.components.CameraComponent
import com.project.apppetstore.ui.components.NoPermissionCard
import com.project.apppetstore.ui.components.RequestPermissionCard
import com.project.apppetstore.utils.createVideoRecordingFile

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatVideoDialog(
    onDismiss: () -> Unit,
    onVideoRecorded: (Uri) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )
    val outputFile = remember { createVideoRecordingFile("APPPETSTORE_VIDEO_${System.currentTimeMillis()}", context) }
    val shouldShowRationale = permissions.permissions.any { permissionState ->
        val status = permissionState.status
        status is PermissionStatus.Denied && status.shouldShowRationale
    }

    LaunchedEffect(Unit) {
        if (!permissions.allPermissionsGranted) {
            permissions.launchMultiplePermissionRequest()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Grabar video",
                style = MaterialTheme.typography.titleMedium
            )

            when {
                permissions.allPermissionsGranted -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    ) {
                        CameraComponent(
                            modifier = Modifier.matchParentSize(),
                            onPhotoTaken = {},
                            isVideoMode = true,
                            videoOutputFile = outputFile,
                            onVideoRecorded = {
                                onVideoRecorded(it)
                                onDismiss()
                            },
                            onVideoError = {
                                Toast.makeText(
                                    context,
                                    "No se pudo grabar el video.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }

                shouldShowRationale -> {
                    RequestPermissionCard(
                        title = "Permiso de camara requerido",
                        message = "Necesitamos la camara para grabar un video desde el chat.",
                        onRequestPermission = { permissions.launchMultiplePermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    NoPermissionCard(
                        message = "La grabacion de video necesita permiso de camara. Puedes habilitarlo desde Ajustes.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cerrar")
            }
        }
    }
}


