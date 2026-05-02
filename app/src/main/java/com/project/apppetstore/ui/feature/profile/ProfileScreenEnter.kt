package com.project.apppetstore.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.project.apppetstore.R
import com.project.apppetstore.ui.components.SecondaryButton

@Composable
fun ProfileScreenEnter(
    userName: String,
    userEmail: String,
    profilePhotoUri: String?,
    petName: String,
    petSpecies: String,
    petAge: String,
    petPhotoUri: String?,
    petTraits: List<String>,
    onEditProfile: () -> Unit,
    onTakePetPhoto: () -> Unit,
    onPickPetPhoto: () -> Unit,
    onSavePetCharacteristics: (String, String, String, List<String>) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nameInput by remember(petName) { mutableStateOf(petName) }
    var speciesInput by remember(petSpecies) { mutableStateOf(petSpecies) }
    var ageInput by remember(petAge) { mutableStateOf(petAge) }
    val selectedTraits = remember(petTraits) { mutableStateListOf<String>().apply { addAll(petTraits) } }
    var showPetPhotoOptions by remember { mutableStateOf(false) }
    val availableTraits = listOf("Jugueton", "Tranquilo", "Sociable", "Energetico", "Carinoso", "Obediente")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))
        // Card usuario
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profilePhotoUri.isNullOrBlank()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_user_round),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    } else {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = userEmail,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(
                    text = "Editar foto de perfil",
                    onClick = onEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Card mascotas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSecondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Mis mascotas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // Hace que ambos hijos tengan el mismo alto
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight() // Ocupa todo el alto disponible
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            if (petPhotoUri.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            } else {
                                AsyncImage(
                                    model = petPhotoUri,
                                    contentDescription = "Foto de mascota",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(nameInput, fontWeight = FontWeight.Bold)
                            Text(
                                "$speciesInput, $ageInput",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                    // Botón Agregar mascota
                    SecondaryButton(
                        onClick = { showPetPhotoOptions = !showPetPhotoOptions },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight() // Ocupa todo el alto disponible
                            .padding(0.dp),
                        content = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_plus),
                                    contentDescription = "Agregar",
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text("Agregar foto", color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                            }
                        }
                    )
                }

                if (showPetPhotoOptions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecondaryButton(
                            text = "Tomar foto",
                            onClick = {
                                showPetPhotoOptions = false
                                onTakePetPhoto()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = "Seleccionar foto",
                            onClick = {
                                showPetPhotoOptions = false
                                onPickPetPhoto()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nombre mascota") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = speciesInput,
                    onValueChange = { speciesInput = it },
                    label = { Text("Especie") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Edad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Caracteristicas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(110.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableTraits) { trait ->
                        val isSelected = selectedTraits.contains(trait)
                        AssistChip(
                            onClick = {
                                if (isSelected) selectedTraits.remove(trait) else selectedTraits.add(trait)
                            },
                            label = { Text(trait) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                TextButton(
                    onClick = {
                        onSavePetCharacteristics(
                            nameInput.trim().ifBlank { "Bobby" },
                            speciesInput.trim().ifBlank { "Perro" },
                            ageInput.trim().ifBlank { "3 anos" },
                            selectedTraits.toList()
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Guardar caracteristicas")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Opciones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column {
                SecondaryButton(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_shopping_bag),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mis pedidos", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                SecondaryButton(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_heart),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Favoritos", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                SecondaryButton(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configuración", fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Botón cerrar sesión
        SecondaryButton(
            text = "Cerrar sesión",
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.error, shape = RoundedCornerShape(8.dp)),
            content = {
                Icon(
                    painter = painterResource(R.drawable.ic_square_arrow_right_exit),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
            }
        )
    }
}
