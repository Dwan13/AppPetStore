package com.project.apppetstore.ui.screens

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.apppetstore.ui.components.CameraComponent
import com.project.apppetstore.ui.theme.AppPetStoreTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InAppCameraScreen(modifier: Modifier = Modifier) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val photos = rememberSaveable { mutableStateListOf<Uri>() }

    LaunchedEffect(Unit) {
        cameraPermission.launchPermissionRequest()
    }

    Column(
        modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (cameraPermission.status.isGranted) {
            OutlinedCard(
                Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
            ) {
                CameraComponent(onPhotoTaken = { photos.add(it) })
            }
            Spacer(Modifier.height(8.dp))
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
            ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(3),
                    verticalItemSpacing = 4.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(items = photos, key = { it.toString() }) { photo ->
                        AsyncImage(model = photo, contentDescription = null)
                    }
                }
            }
        } else {
            if (cameraPermission.status.shouldShowRationale) {
                Text("Necesitamos permiso de camara para tomar la foto")
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Solicitar permiso")
                }
            } else {
                Text("Sin permisos de camara")
            }
        }
    }
}

@PreviewLightDark
@Composable
fun InAppCameraScreenPreview() {
    AppPetStoreTheme {
        InAppCameraScreen()
    }
}