package com.project.adopetshop.ui.feature.adoption

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.project.adopetshop.R
import com.project.adopetshop.data.model.ChatMessage
import com.project.adopetshop.data.model.Pet
import com.project.adopetshop.ui.components.PrimaryButton
import kotlinx.coroutines.delay
import com.project.adopetshop.ui.components.ChatSection

@Composable
fun PetDetailScreen(
    pet: Pet,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAdoptClick: () -> Unit,
    onBack: () -> Unit,

    // 👇 conectar chat real
    messages: List<ChatMessage>,
    currentInput: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {

    var showChat by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    // Scroll automático al chat informativo al entrar
    LaunchedEffect(Unit) {
        delay(350) // Espera breve para que se componga la UI
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp) // Deja espacio para el botón fijo
        ) {
            // Imagen superior con botones
            Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                when {
                    pet.imageUrl != null -> {
                        // Si hay URL, usa AsyncImage
                        AsyncImage(
                            model = pet.imageUrl,
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            fallback = rememberVectorPainter(Icons.Default.Face),
                            error = rememberVectorPainter(Icons.Default.Clear),
                        )
                    }
                    pet.imageRes != null -> {
                        // Si hay recurso local, usa Image
                        Image(
                            painter = painterResource(pet.imageRes),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
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
                // Botón back (más visible y por encima)
                Icon(
                    painter = painterResource(R.drawable.ic_move_left),
                    contentDescription = "Volver",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onBack() },
                    tint = Color.Black
                )
                // Botón heart
                Icon(
                    painter = painterResource(R.drawable.ic_heart),
                    contentDescription = "Favorito",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        .clickable { onToggleFavorite() },
                    tint = if (isFavorite) Color(0xFFFF5A5F) else Color(0xFFB0B0B0)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Card de datos principales
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .background(Color.White, shape = RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE5E7EB), shape = RoundedCornerShape(18.dp))
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
            // Chat informativo
            Spacer(modifier = Modifier.height(16.dp))

            ChatSection(
                messages = messages,
                currentInput = currentInput,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        // Botón adoptar fijo en la parte inferior
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(0.dp))
                .padding(bottom = 16.dp, top = 8.dp)
        ) {
            PrimaryButton(
                text = "Iniciar proceso de adopción",
                onClick = onAdoptClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
    }
}
