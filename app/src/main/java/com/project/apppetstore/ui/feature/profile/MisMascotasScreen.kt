package com.project.apppetstore.ui.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.apppetstore.R
import com.project.apppetstore.data.model.UserPet
import com.project.apppetstore.ui.components.SecondaryButton

private val MM_AVAILABLE_TRAITS = listOf(
    "Jugueton", "Tranquilo", "Sociable", "Energetico", "Carinoso", "Obediente"
)
private val MM_SPECIES_OPTIONS = listOf("Perro", "Gato")
private val MM_GENDER_OPTIONS = listOf("Macho", "Hembra")
private val MM_SIZE_OPTIONS = listOf("Pequeno", "Mediano", "Grande")

// ── Data class para solicitudes de adopción ──────────────────────────────────
data class AdoptionRequest(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val chatId: String = "",
    val timestamp: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MisMascotasScreen(
    uiState: PetsUiState,
    onAddPet: (name: String, species: String, age: String, gender: String, size: String, health: String, vaccines: String, requirements: String, traits: List<String>, photoUri: Uri?) -> Unit,
    onUpdatePet: (petId: String, name: String, species: String, age: String, gender: String, size: String, health: String, vaccines: String, requirements: String, traits: List<String>, photoUri: Uri?) -> Unit,
    onDeletePet: (petId: String) -> Unit,
    onToggleAdoption: (petId: String) -> Unit,
    onClearResult: () -> Unit,
    onBack: () -> Unit,
    onOpenAdoptionChat: (chatId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pets = uiState.pets
    val isLoading = uiState.isLoading
    val isSaving = uiState.isSaving
    val result = uiState.operationResult

    var showSheet by remember { mutableStateOf(false) }
    var editingPet by remember { mutableStateOf<UserPet?>(null) }
    var petToDelete by remember { mutableStateOf<UserPet?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Solicitudes de adopción (notificaciones tipo "pet") ─────────────────────
    var adoptionRequests by remember { mutableStateOf<List<AdoptionRequest>>(emptyList()) }
    var isLoadingRequests by remember { mutableStateOf(true) }

    // ── Cerrar sheet automáticamente cuando Firebase termine
    var waitingForSave by remember { mutableStateOf(false) }
    LaunchedEffect(isSaving) {
        if (waitingForSave && !isSaving) {
            showSheet = false
            waitingForSave = false
        }
    }

    // ── Escuchar solicitudes de adopción en tiempo real ───────────────────────
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    DisposableEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            adoptionRequests = emptyList()
            isLoadingRequests = false
            onDispose { }
        } else {
            val reg = firestore.collection("users").document(uid)
                .collection("notifications")
                .whereEqualTo("type", "pet")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        isLoadingRequests = false
                        return@addSnapshotListener
                    }

                    val raw = snap?.documents?.mapNotNull { doc ->
                        runCatching {
                            val cId = doc.getString("chatId") ?: ""
                            val resolved = doc.getBoolean("resolved") ?: false
                            if (resolved) return@runCatching null
                            if (cId.isBlank()) return@runCatching null
                            AdoptionRequest(
                                id = doc.id,
                                title = doc.getString("title") ?: "Solicitud de adopción",
                                message = doc.getString("message") ?: "Nuevo mensaje",
                                chatId = cId,
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        }.getOrNull()
                    } ?: emptyList()

                    val dedupByChat = raw
                        .groupBy { it.chatId }
                        .mapNotNull { (_, items) -> items.maxByOrNull { it.timestamp } }
                        .sortedByDescending { it.timestamp }

                    adoptionRequests = dedupByChat
                    isLoadingRequests = false
                }

            onDispose { reg.remove() }
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editingPet = null; showSheet = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Agregar mascota") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Mis mascotas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (pets.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "(${pets.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider()

            // ── Solicitudes de adopción (siempre visible para que el dueño la encuentre) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubble,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Solicitudes de adopción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (adoptionRequests.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = adoptionRequests.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                when {
                    isLoadingRequests -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Cargando solicitudes...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    adoptionRequests.isEmpty() -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "Aún no tienes solicitudes de adopción. Cuando alguien te escriba por una mascota, aparecerá aquí.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(adoptionRequests, key = { it.id }) { request ->
                                AdoptionRequestCard(
                                    request = request,
                                    onClick = { onOpenAdoptionChat(request.chatId) }
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Cargando mascotas…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                pets.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                text = "Sin mascotas registradas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Toca el botón para agregar\ntu primera mascota",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pets, key = { it.id }) { pet ->
                            MisMascotasPetCard(
                                pet = pet,
                                isTogglingAdopt = pet.id in uiState.togglingAdoptionIds,
                                onEdit = { editingPet = pet; showSheet = true },
                                onDelete = { petToDelete = pet },
                                onToggleAdoption = { onToggleAdoption(pet.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Diálogo de resultado Firebase ─────────────────────────────────────────
    if (result != null) {
        val isSuccess = result is PetOperationResult.Success
        val msg = when (result) {
            is PetOperationResult.Success -> result.message
            is PetOperationResult.Failure -> result.message
        }

        AlertDialog(
            onDismissRequest = onClearResult,
            icon = {
                Icon(
                    imageVector = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isSuccess) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (isSuccess) "¡Operación exitosa!" else "Algo salió mal",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = msg,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onClearResult,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // ── Diálogo de confirmación de borrado ────────────────────────────────────
    petToDelete?.let { pet ->
        AlertDialog(
            onDismissRequest = { petToDelete = null },
            title = { Text("Eliminar mascota") },
            text = { Text("¿Seguro que quieres eliminar a ${pet.name}?") },
            confirmButton = {
                TextButton(onClick = { onDeletePet(pet.id); petToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { petToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    // ── Bottom sheet: agregar / editar ────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { if (!isSaving) showSheet = false },
            sheetState = sheetState
        ) {
            MisMascotasFormSheet(
                pet = editingPet,
                existingPets = pets,
                isSaving = isSaving,
                onSave = { name, species, age, gender, size, health, vaccines, requirements, traits, photoUri ->
                    waitingForSave = true
                    if (editingPet == null)
                        onAddPet(
                            name,
                            species,
                            age,
                            gender,
                            size,
                            health,
                            vaccines,
                            requirements,
                            traits,
                            photoUri
                        )
                    else
                        onUpdatePet(
                            editingPet!!.id,
                            name,
                            species,
                            age,
                            gender,
                            size,
                            health,
                            vaccines,
                            requirements,
                            traits,
                            photoUri
                        )
                },
                onCancel = { if (!isSaving) showSheet = false }
            )
        }
    }
}

 // Tarjeta de mascota
 
@Composable
private fun MisMascotasPetCard(
    pet: UserPet,
    isTogglingAdopt: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAdoption: () -> Unit
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {

            // Foto o inicial
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!pet.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = pet.photoUri,
                        contentDescription = pet.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = pet.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = pet.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${pet.species}${if (pet.age.isNotBlank()) " · ${pet.age}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (pet.gender.isNotBlank() || pet.size.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(
                        pet.gender.takeIf { it.isNotBlank() },
                        pet.size.takeIf { it.isNotBlank() }).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (pet.traits.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pet.traits.take(2).joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Botón "Dar en adopción" ──────────────────────────────────────
            val isAdoption = pet.isAvailableForAdoption
            Surface(
                onClick = { if (!isTogglingAdopt) onToggleAdoption() },
                shape = MaterialTheme.shapes.small,
                color = when {
                    isTogglingAdopt -> MaterialTheme.colorScheme.surfaceVariant
                    isAdoption -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isTogglingAdopt) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = if (isAdoption) Icons.Rounded.Favorite
                            else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isAdoption) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = when {
                            isTogglingAdopt -> if (isAdoption) "Retirando…" else "Publicando…"
                            isAdoption -> "En adopción"
                            else -> "Dar en adopción"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAdoption || isTogglingAdopt) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

 // Formulario de mascota (Bottom Sheet)
 
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MisMascotasFormSheet(
    pet: UserPet?,
    existingPets: List<UserPet>,
    isSaving: Boolean,
    onSave: (name: String, species: String, age: String, gender: String, size: String, health: String, vaccines: String, requirements: String, traits: List<String>, photoUri: Uri?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(pet) { mutableStateOf(pet?.name ?: "") }
    var species by remember(pet) {
        mutableStateOf(
            pet?.species?.let { if (it in MM_SPECIES_OPTIONS) it else MM_SPECIES_OPTIONS[0] }
                ?: MM_SPECIES_OPTIONS[0]
        )
    }
    var age by remember(pet) { mutableStateOf(pet?.age ?: "") }
    var gender by remember(pet) {
        mutableStateOf(pet?.gender?.takeIf { it in MM_GENDER_OPTIONS } ?: MM_GENDER_OPTIONS[0])
    }
    var size by remember(pet) {
        mutableStateOf(pet?.size?.takeIf { it in MM_SIZE_OPTIONS } ?: MM_SIZE_OPTIONS[1])
    }
    var health by remember(pet) { mutableStateOf(pet?.health ?: "") }
    var vaccines by remember(pet) { mutableStateOf(pet?.vaccines ?: "") }
    var requirements by remember(pet) { mutableStateOf(pet?.requirements ?: "") }
    val selectedTraits = remember(pet) {
        mutableStateListOf<String>().apply { addAll(pet?.traits ?: emptyList()) }
    }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedPhotoUri = uri }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = if (pet == null) "Agregar mascota" else "Editar mascota",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // ── Foto ──────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(enabled = !isSaving) {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            val photoToShow = selectedPhotoUri?.toString() ?: pet?.photoUri
            if (!photoToShow.isNullOrBlank()) {
                AsyncImage(
                    model = photoToShow,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_user_round),
                    contentDescription = "Elegir foto",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Text(
            text = "Toca para cambiar la foto",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ── Nombre ────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text("Nombre *") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            enabled = !isSaving,
            isError = nameError != null,
            supportingText = nameError?.let { msg ->
                { Text(msg, color = MaterialTheme.colorScheme.error) }
            }
        )

        // ── Especie ───────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Especie", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                MM_SPECIES_OPTIONS.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = species == option,
                        onClick = { if (!isSaving) species = option },
                        shape = SegmentedButtonDefaults.itemShape(index, MM_SPECIES_OPTIONS.size),
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Pets,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(option)
                            }
                        }
                    )
                }
            }
        }

        // ── Edad ──────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Edad (ej: 2 años)") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            enabled = !isSaving
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Género", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                MM_GENDER_OPTIONS.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = gender == option,
                        onClick = { if (!isSaving) gender = option },
                        shape = SegmentedButtonDefaults.itemShape(index, MM_GENDER_OPTIONS.size),
                        label = { Text(option) }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Tamaño", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                MM_SIZE_OPTIONS.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = size == option,
                        onClick = { if (!isSaving) size = option },
                        shape = SegmentedButtonDefaults.itemShape(index, MM_SIZE_OPTIONS.size),
                        label = { Text(option) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = health,
            onValueChange = { health = it },
            label = { Text("Estado de salud") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            enabled = !isSaving,
            minLines = 2,
            maxLines = 4
        )

        OutlinedTextField(
            value = vaccines,
            onValueChange = { vaccines = it },
            label = { Text("Vacunas") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            enabled = !isSaving,
            minLines = 1,
            maxLines = 3
        )

        OutlinedTextField(
            value = requirements,
            onValueChange = { requirements = it },
            label = { Text("Requisitos de adopción") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            enabled = !isSaving,
            minLines = 2,
            maxLines = 5
        )

        // ── Características ───────────────────────────────────────────────────
        Text("Características", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MM_AVAILABLE_TRAITS.forEach { trait ->
                val selected = trait in selectedTraits
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (!isSaving) {
                            if (selected) selectedTraits.remove(trait)
                            else selectedTraits.add(trait)
                        }
                    },
                    label = { Text(trait, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        // ── Botones ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryButton(
                text = "Cancelar",
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            )
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    when {
                        trimmedName.isBlank() -> {
                            nameError = "El nombre no puede estar vacío"
                        }

                        existingPets.any { e ->
                            e.name.trim().equals(trimmedName, ignoreCase = true) &&
                                    e.id != (pet?.id ?: "")
                        } -> {
                            nameError = "Ya tienes una mascota llamada \"$trimmedName\""
                        }

                        else -> onSave(
                            trimmedName,
                            species,
                            age.trim(),
                            gender,
                            size,
                            health.trim(),
                            vaccines.trim(),
                            requirements.trim(),
                            selectedTraits.toList(),
                            selectedPhotoUri
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (pet == null) "Guardar" else "Actualizar")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

 // Tarjeta de solicitud de adopción
 
@Composable
private fun AdoptionRequestCard(
    request: AdoptionRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = request.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = request.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Botón de acción ──────────────────────────────────────────────
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Ver solicitud")
            }
        }
    }
}

