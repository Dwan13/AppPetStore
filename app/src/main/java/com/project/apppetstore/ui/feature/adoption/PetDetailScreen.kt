package com.project.apppetstore.ui.feature.adoption

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.project.apppetstore.R
import com.project.apppetstore.data.model.ChatMessage
import com.project.apppetstore.data.model.ChatAttachment
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.ui.components.PrimaryButton
import kotlinx.coroutines.delay
import com.project.apppetstore.ui.components.ChatSection

@Composable
fun PetDetailScreen(
    pet: Pet,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAdoptClick: () -> Unit,
    onBack: () -> Unit,

    // conectar chat real
    messages: List<ChatMessage>,
    currentInput: String,
    pendingAttachment: ChatAttachment?,
    isUploading: Boolean = false,
    isLoadingMessages: Boolean = false,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onDeleteMessage: (String) -> Unit,
    onRemovePendingAttachment: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
    onRecordVideo: () -> Unit,
    onPickVideo: () -> Unit,
    onRecordAudio: () -> Unit,
    onPickAudio: () -> Unit,
    // UC-26: valores del giroscopio suavizados para el parallax
    gyroX: Float = 0f,
    gyroY: Float = 0f,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    // UC-26: animar el desplazamiento con spring para eliminar jitter
    val parallaxOffsetX by animateFloatAsState(
        targetValue = (gyroY * 28f).coerceIn(-14f, 14f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "parallaxX"
    )
    val parallaxOffsetY by animateFloatAsState(
        targetValue = (gyroX * 20f).coerceIn(-10f, 10f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "parallaxY"
    )

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        delay(350)
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                when {
                    pet.imageUrl != null -> {
                        AsyncImage(
                            model = pet.imageUrl,
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    // UC-26: parallax — escala ligeramente para evitar bordes vacíos
                                    scaleX = 1.06f
                                    scaleY = 1.06f
                                    translationX = parallaxOffsetX * density
                                    translationY = parallaxOffsetY * density
                                },
                            fallback = rememberVectorPainter(Icons.Default.Face),
                            error = rememberVectorPainter(Icons.Default.Clear),
                        )
                    }
                    pet.imageRes != null -> {
                        Image(
                            painter = painterResource(pet.imageRes),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = 1.06f
                                    scaleY = 1.06f
                                    translationX = parallaxOffsetX * density
                                    translationY = parallaxOffsetY * density
                                },
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF3FCD6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pet.name.take(1), fontSize = 80.sp, color = Color(0xFF5C9639), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_move_left),
                        contentDescription = "Volver",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart),
                        contentDescription = "Favorito",
                        modifier = Modifier.size(18.dp),
                        tint = if (isFavorite) Color(0xFFFF5A5F) else Color(0xFFB0B0B0)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("• Disponible", color = Color(0xFF5C9639), fontSize = 15.sp)
                    }
                    Text(pet.breed, color = Color(0xFF6B7280), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column { Text("Edad", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(pet.age, fontSize = 13.sp) }
                        Column { Text("Género", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(pet.gender, fontSize = 13.sp) }
                        Column { Text("Tamaño", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(pet.size, fontSize = 13.sp) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Salud, vacunas, personalidad, requisitos
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• Salud", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF5C9639))
                        Text(pet.health, fontSize = 13.sp)
                        Text("• Vacunas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF5C9639))
                        Text(pet.vaccines, fontSize = 13.sp)
                        Text("• Personalidad", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF5C9639))
                        Text(pet.personality, fontSize = 13.sp)
                        Text("• Requisitos", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF5C9639))
                        Text(pet.requirements, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Banner contextual según tipo de mascota
            if (pet.ownerUid != null) {
                // Mascota de usuario: el chat ES el proceso de adopción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(
                            color = Color(0xFFEAFBC1),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF5C9639)
                    )
                    Text(
                        text = "Escribe un mensaje al dueño para iniciar la adopción",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2D5A1A)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            ChatSection(
                messages = messages,
                currentInput = currentInput,
                pendingAttachment = pendingAttachment,
                isUploading = isUploading,
                isLoadingMessages = isLoadingMessages,
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
