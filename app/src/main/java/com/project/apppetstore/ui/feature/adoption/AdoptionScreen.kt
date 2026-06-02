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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Wc
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.apppetstore.R
import com.project.apppetstore.data.model.AttachmentType
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.ui.components.AppAsyncImage
import com.project.apppetstore.ui.components.ChatSection
import com.project.apppetstore.ui.components.PetCard
import com.project.apppetstore.ui.feature.favorites.FavoritesViewModel
import com.project.apppetstore.ui.viewmodels.SensorViewModel
import com.project.apppetstore.utils.createVideoOnMoviesFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionScreen(
    uiState: AdoptionUiState,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onDeleteMessage: (String) -> Unit,
    onAttachMedia: (AttachmentType, String) -> Unit,
    onRemovePendingAttachment: () -> Unit,
    modifier: Modifier = Modifier,
    selectedPetId: String? = null,
    onPetSelected: (String) -> Unit = {},
    // UC-25: navegar al detalle de la mascota sorpresa
    onNavigateToPet: (String) -> Unit = {},
    onBack: () -> Unit = {},
    // UC-25/26: sensor ViewModel compartido con PetDetailScreen
    sensorViewModel: SensorViewModel = viewModel(),
    // Favoritos — ViewModel con persistencia real en Firestore
    favoritesViewModel: FavoritesViewModel = viewModel(),
    // true cuando el dueño abre el chat directo desde una notificación
    isOwnerViewingChat: Boolean = false
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    // ── UC-25/26: setup del sensor en esta pantalla ───────────────────────────
    val sensorState by sensorViewModel.state.collectAsState()
    DisposableEffect(Unit) {
        sensorViewModel.setup(context)
        sensorViewModel.startListening()
        onDispose { sensorViewModel.stopListening() }
    }

    // ── UC-25: escuchar el evento de "agitar" ─────────────────────────────────
    var shakeDiscoverPet by remember { mutableStateOf<Pet?>(null) }
    LaunchedEffect(sensorViewModel) {
        sensorViewModel.shakeEvent.collect {
            // Solo mostrar sorpresa en la vista de lista (no en detalle)
            if (selectedPetId == null && uiState.pets.isNotEmpty()) {
                shakeDiscoverPet = uiState.pets.random()
            }
        }
    }

    val petsToShow = if (selectedPetId != null) uiState.pets.filter { it.id == selectedPetId } else uiState.pets
    // isFavorite deriva del estado real de Firestore (FavoritesViewModel)
    val favoritesState = favoritesViewModel.uiState
    val isFavorite = selectedPetId != null && selectedPetId in favoritesState.favoritePetIds
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

    // ── Vista del dueño: chat directo desde notificación ────────────────────
    if (isOwnerViewingChat) {
        OwnerChatView(
            uiState                   = uiState,
            onBack                    = onBack,
            onInputChange             = onInputChange,
            onSendMessage             = onSendMessage,
            onDeleteMessage           = onDeleteMessage,
            onRemovePendingAttachment = onRemovePendingAttachment,
            onTakePhoto               = onTakePhoto,
            onPickImage               = onPickImage,
            onRecordVideo             = onRecordVideo,
            onPickVideo               = onPickVideo,
            onRecordAudio             = onRecordAudio,
            onPickAudio               = onPickAudio,
            modifier                  = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .then(
                // En detalle de mascota quitamos el padding lateral para que ocupe todo el ancho
                if (selectedPet == null)
                    Modifier.padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 8.dp)
                else
                    Modifier
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedPet != null) {
            // Pantalla de detalle con datos
            PetDetailScreen(
                pet = selectedPet,
                isFavorite = isFavorite,
                onToggleFavorite = { selectedPetId?.let { favoritesViewModel.toggleFavorite(it) } },
                onAdoptClick = { },
                onBack = onBack,

                messages = uiState.messages,
                currentInput = uiState.currentInput,
                pendingAttachment = uiState.pendingAttachment,
                isUploading = uiState.isUploading,
                isLoadingMessages = uiState.isLoadingMessages,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage,
                onDeleteMessage = onDeleteMessage,
                onRemovePendingAttachment = onRemovePendingAttachment,
                onTakePhoto = onTakePhoto,
                onPickImage = onPickImage,
                onRecordVideo = onRecordVideo,
                onPickVideo = onPickVideo,
                onRecordAudio = onRecordAudio,
                onPickAudio = onPickAudio,
                // UC-26: giroscopio para el parallax del hero
                gyroX = sensorState.gyroX,
                gyroY = sensorState.gyroY
            )

        } else {
            // ...existing list view code...
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
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
                        .padding(vertical = 4.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .fillMaxWidth()
                        .clickable {
                            highlightedPetIndex = index
                            onPetSelected(pet.id)
                        },
                    image           = pet.imageRes?.let { painterResource(it) },
                    isFavorite      = pet.id in favoritesState.favoritePetIds,
                    onFavoriteClick = { favoritesViewModel.toggleFavorite(pet.id) }
                )
            }

            ChatSection(
                messages = uiState.messages,
                currentInput = uiState.currentInput,
                pendingAttachment = uiState.pendingAttachment,
                isUploading = uiState.isUploading,
                isLoadingMessages = uiState.isLoadingMessages,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage,
                onDeleteMessage = onDeleteMessage,
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

    // "Agitar para descubrir"
    shakeDiscoverPet?.let { pet ->
        ShakeDiscoverBottomSheet(
            pet       = pet,
            onDismiss = { shakeDiscoverPet = null },
            onViewProfile = {
                shakeDiscoverPet = null
                onNavigateToPet(pet.id)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vista del dueño: responde a solicitudes de adopción desde notificación
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OwnerChatView(
    uiState                   : AdoptionUiState,
    onBack                    : () -> Unit,
    onInputChange             : (String) -> Unit,
    onSendMessage             : () -> Unit,
    onDeleteMessage           : (String) -> Unit,
    onRemovePendingAttachment : () -> Unit,
    onTakePhoto               : () -> Unit,
    onPickImage               : () -> Unit,
    onRecordVideo             : () -> Unit,
    onPickVideo               : () -> Unit,
    onRecordAudio             : () -> Unit,
    onPickAudio               : () -> Unit,
    modifier                  : Modifier = Modifier
) {
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    val tt = androidx.compose.material3.MaterialTheme.typography

    Column(modifier = modifier.fillMaxSize()) {
        // ── TopBar ──────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(onClick = onBack) {
                Icon(
                    imageVector        = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Column {
                androidx.compose.material3.Text(
                    text  = "Solicitud de adopción",
                    style = tt.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.material3.Text(
                    text  = "Un usuario está interesado en tu mascota",
                    style = tt.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
        }
        androidx.compose.material3.HorizontalDivider()

        // ── Chat ─────────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            ChatSection(
                messages                  = uiState.messages,
                currentInput              = uiState.currentInput,
                pendingAttachment         = uiState.pendingAttachment,
                isUploading               = uiState.isUploading,
                isLoadingMessages         = uiState.isLoadingMessages,
                onInputChange             = onInputChange,
                onSendMessage             = onSendMessage,
                onDeleteMessage           = onDeleteMessage,
                onRemovePendingAttachment = onRemovePendingAttachment,
                onTakePhoto               = onTakePhoto,
                onPickImage               = onPickImage,
                onRecordVideo             = onRecordVideo,
                onPickVideo               = onPickVideo,
                onRecordAudio             = onRecordAudio,
                onPickAudio               = onPickAudio
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// "Mascota sorpresa"
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShakeDiscoverBottomSheet(
    pet: Pet,
    onDismiss: () -> Unit,
    onViewProfile: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "¡Mascota sorpresa!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "Sacudiste el teléfono — aquí está tu match:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Foto de la mascota
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !pet.imageUrl.isNullOrBlank() ->
                        AppAsyncImage(
                            imageUrl = pet.imageUrl,
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(140.dp).clip(CircleShape),
                            placeholderRes = R.drawable.ic_user_round
                        )
                    pet.imageRes != null -> {
                        androidx.compose.foundation.Image(
                            painter = painterResource(pet.imageRes),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(140.dp).clip(CircleShape)
                        )
                    }
                    else ->
                        Text(
                            pet.name.take(1).uppercase(),
                            fontSize = 56.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                }
            }

            // Nombre
            Text(
                text = pet.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Detalles rápidos
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pet.age.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Cake, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pet.age, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (pet.breed.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Pets, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pet.breed, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (pet.gender.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Wc, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pet.gender, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (pet.size.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Height, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pet.size, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (pet.personality.isNotBlank()) {
                Text(
                    text = "\"${pet.personality}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Seguir viendo")
                }
                Button(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver perfil")
                }
            }
        }
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

