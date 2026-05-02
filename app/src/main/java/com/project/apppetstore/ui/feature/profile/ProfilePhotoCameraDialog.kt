package com.project.apppetstore.ui.feature.profile

import android.Manifest
import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.project.apppetstore.ui.components.CameraComponent
import com.project.apppetstore.ui.components.NoPermissionCard
import com.project.apppetstore.ui.components.RequestPermissionCard

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfilePhotoCameraDialog(
    onDismiss: () -> Unit,
    onPhotoTaken: (Uri) -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
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
                text = "Editar foto de perfil",
                style = MaterialTheme.typography.titleMedium
            )

            when (val status = cameraPermission.status) {
                PermissionStatus.Granted -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    ) {
                        CameraComponent(
                            modifier = Modifier.matchParentSize(),
                            onPhotoTaken = {
                                onPhotoTaken(it)
                                onDismiss()
                            }
                        )
                    }
                }

                is PermissionStatus.Denied -> {
                    RequestPermissionCard(
                        onRequestPermission = { cameraPermission.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!status.shouldShowRationale) {
                        NoPermissionCard(modifier = Modifier.fillMaxWidth())
                    }
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

