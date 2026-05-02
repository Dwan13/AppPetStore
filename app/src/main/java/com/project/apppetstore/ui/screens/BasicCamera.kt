package com.project.apppetstore.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.apppetstore.ui.theme.AppPetStoreTheme
import com.project.apppetstore.utils.createTempImageFileInInternalPicturesFolder

@Composable
fun BasicCameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val photos = rememberSaveable { mutableStateListOf<Uri>() }
    var tmpImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val systemCameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success) {
                    tmpImageUri?.let { photos.add(it) }
                }
            }
        )

    Box(
        modifier
            .padding(20.dp)
            .fillMaxSize()
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
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

        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier
                    .height(80.dp)
                    .width(160.dp),
                onClick = {
                    val name = "PHOTOBOOTH_IMG_${System.currentTimeMillis()}"
                    val tmpUri = createTempImageFileInInternalPicturesFolder(name, context)
                    tmpImageUri = tmpUri
                    systemCameraLauncher.launch(tmpUri)
                }
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun BasicCameraScreenPreview() {
    AppPetStoreTheme {
        BasicCameraScreen()
    }
}