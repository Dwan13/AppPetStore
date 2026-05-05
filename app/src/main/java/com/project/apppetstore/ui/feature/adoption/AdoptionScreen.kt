package com.project.apppetstore.ui.feature.adoption

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.project.apppetstore.data.model.AttachmentType
import com.project.apppetstore.ui.components.ChatSection
import com.project.apppetstore.ui.components.PetCard
import com.project.apppetstore.utils.createVideoOnMoviesFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionScreen(
    uiState: AdoptionUiState,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachMedia: (AttachmentType, String) -> Unit,
    onRemovePendingAttachment: () -> Unit,
    modifier: Modifier = Modifier,
    selectedPetId: String? = null,
    onPetSelected: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    val petsToShow = if (selectedPetId != null) uiState.pets.filter { it.id == selectedPetId } else uiState.pets
    var isFavorite by remember(selectedPetId) { mutableStateOf(false) }
    val selectedPet = uiState.pets.find { it.id == selectedPetId }
    var showCameraDialog by remember { mutableStateOf(false) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }
    var highlightedPetIndex by remember { mutableStateOf(0) }
    val petsCarouselState = rememberCarouselState { petsToShow.size }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { onAttachMedia(AttachmentType.IMAGE, it.toString()) }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { onAttachMedia(AttachmentType.VIDEO, it.toString()) }
        }
    )

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { onAttachMedia(AttachmentType.AUDIO, it.toString()) }
        }
    )

    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            val outputUri = pendingVideoUri
            pendingVideoUri = null

            if (success && outputUri != null) {
                onAttachMedia(AttachmentType.VIDEO, outputUri.toString())
            }
        }
    )

    val audioRecordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val recordedUri = result.data?.data
            if (recordedUri != null) {
                onAttachMedia(AttachmentType.AUDIO, recordedUri.toString())
            } else {
                Toast.makeText(
                    context,
                    "No se pudo obtener el audio grabado. Selecciona un audio.",
                    Toast.LENGTH_SHORT
                ).show()
                audioPickerLauncher.launch("audio/*")
            }
        }
    )

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val recordIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                if (recordIntent.resolveActivity(packageManager) != null) {
                    try {
                        audioRecordLauncher.launch(recordIntent)
                    } catch (_: Exception) {
                        launchAudioPickerFallback(context, audioPickerLauncher)
                    }
                } else {
                    launchAudioPickerFallback(context, audioPickerLauncher)
                }
            } else {
                Toast.makeText(
                    context,
                    "Permiso de audio denegado. Selecciona un audio.",
                    Toast.LENGTH_SHORT
                ).show()
                audioPickerLauncher.launch("audio/*")
            }
        }
    )

    val onTakePhoto = { showCameraDialog = true }
    val onPickImage = {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    val onPickVideo = {
        videoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }
    val onPickAudio = {
        audioPickerLauncher.launch("audio/*")
    }
    val onRecordVideo = {
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (captureVideoIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(
                context,
                "No hay app de video. Selecciona un video.",
                Toast.LENGTH_SHORT
            ).show()
            onPickVideo()
        } else {
            val outputUri = createVideoOnMoviesFolder(
                name = "APPPETSTORE_VIDEO_${System.currentTimeMillis()}",
                context = context
            )

            if (outputUri == null) {
                Toast.makeText(
                    context,
                    "No se pudo preparar la grabacion de video. Selecciona un video.",
                    Toast.LENGTH_SHORT
                ).show()
                onPickVideo()
            } else {
                pendingVideoUri = outputUri
                try {
                    videoCaptureLauncher.launch(outputUri)
                } catch (_: ActivityNotFoundException) {
                    pendingVideoUri = null
                    Toast.makeText(
                        context,
                        "No hay camara de video disponible. Selecciona un video.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onPickVideo()
                } catch (_: Exception) {
                    pendingVideoUri = null
                    Toast.makeText(
                        context,
                        "No se pudo abrir la camara de video. Selecciona un video.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onPickVideo()
                }
            }
        }
    }
    val onRecordAudio = {
        val recordIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        if (recordIntent.resolveActivity(packageManager) == null) {
            launchAudioPickerFallback(context, audioPickerLauncher)
        } else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    audioRecordLauncher.launch(recordIntent)
                } catch (_: ActivityNotFoundException) {
                    launchAudioPickerFallback(context, audioPickerLauncher)
                } catch (_: Exception) {
                    launchAudioPickerFallback(context, audioPickerLauncher)
                }
            } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedPet != null) {
            // Pantalla de detalle con datos
            PetDetailScreen(
                pet = selectedPet,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite },
                onAdoptClick = { },
                onBack = onBack,

                messages = uiState.messages,
                currentInput = uiState.currentInput,
                pendingAttachment = uiState.pendingAttachment,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage,
                onRemovePendingAttachment = onRemovePendingAttachment,
                onTakePhoto = onTakePhoto,
                onPickImage = onPickImage,
                onRecordVideo = onRecordVideo,
                onPickVideo = onPickVideo,
                onRecordAudio = onRecordAudio,
                onPickAudio = onPickAudio
            )

        } else {
            Text(
                "Chat de Adopción",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            HorizontalMultiBrowseCarousel(
                state = petsCarouselState,
                modifier = Modifier.fillMaxWidth(),
                preferredItemWidth = 220.dp,
                itemSpacing = 10.dp,
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) { index ->
                val pet = petsToShow[index]
                val isActive = highlightedPetIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.93f,
                    label = "adoption_pet_card_scale"
                )
                PetCard(
                    pet = pet,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .fillMaxWidth()
                        .clickable {
                            highlightedPetIndex = index
                            onPetSelected(pet.id)
                        },
                    image = pet.imageRes?.let { painterResource(it) }
                )
            }

            ChatSection(
                messages = uiState.messages,
                currentInput = uiState.currentInput,
                pendingAttachment = uiState.pendingAttachment,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage,
                onRemovePendingAttachment = onRemovePendingAttachment,
                onTakePhoto = onTakePhoto,
                onPickImage = onPickImage,
                onRecordVideo = onRecordVideo,
                onPickVideo = onPickVideo,
                onRecordAudio = onRecordAudio,
                onPickAudio = onPickAudio
            )
        }
    }

    if (showCameraDialog) {
        ChatCameraDialog(
            onDismiss = { showCameraDialog = false },
            onPhotoTaken = { uri -> onAttachMedia(AttachmentType.IMAGE, uri.toString()) }
        )
    }
}

private fun launchAudioPickerFallback(
    context: android.content.Context,
    audioPickerLauncher: ActivityResultLauncher<String>
) {
    Toast.makeText(
        context,
        "Este dispositivo no permite grabar audio. Selecciona un audio.",
        Toast.LENGTH_SHORT
    ).show()
    audioPickerLauncher.launch("audio/*")
}

