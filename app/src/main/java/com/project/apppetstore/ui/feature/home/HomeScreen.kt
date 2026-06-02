package com.project.apppetstore.ui.feature.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.project.apppetstore.R
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.ui.components.FilterChipWithIcon
import com.project.apppetstore.ui.components.HomeContentSkeleton
import com.project.apppetstore.ui.components.PetCard
import com.project.apppetstore.ui.components.SectionTitle
import com.project.apppetstore.ui.components.ServiceCard
import com.project.apppetstore.ui.components.ServiceCardSkeleton
import com.project.apppetstore.ui.feature.map.MapScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onPetClick: ((petId: String) -> Unit)? = null,
    onFilterSelected: (String) -> Unit = {},
    onScheduleService: (Service) -> Unit = {},
    onLocationAvailable: (Double, Double) -> Unit = { _, _ -> },
    favoritePetIds: Set<String> = emptySet(),
    onFavoriteToggle: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val imageLoader = remember { SingletonImageLoader.get(context) }

    // ── Obtener ubicación si el permiso ya está concedido ─────────────────────
    fun tryFetchLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) onLocationAvailable(loc.latitude, loc.longitude)
                }
                .addOnFailureListener {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedClient.lastLocation.addOnSuccessListener { loc ->
                            if (loc != null) onLocationAvailable(loc.latitude, loc.longitude)
                        }
                    }
                }
        }
    }

    // Lanzador de permiso: si el usuario lo concede, obtenemos la ubicación de inmediato
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) tryFetchLocation()
    }

    // Al entrar en pantalla: si ya tenemos permiso lo usamos; si no, lo solicitamos
    LaunchedEffect(Unit) {
        val already = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (already) tryFetchLocation()
        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // ── Estado local ──────────────────────────────────────────────────────────
    var showMapSheet by remember { mutableStateOf(false) }
    val mapSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val homeListState = rememberLazyListState()
    val filterListState = rememberLazyListState()

    var highlightedPetIndex by rememberSaveable { mutableStateOf(0) }

    // Prefetch progresivo: adelanta imágenes cercanas al scroll para reducir parpadeos.
    LaunchedEffect(homeListState, uiState.isLoading, uiState.isServicesLoading, uiState.services) {
        if (uiState.isLoading || uiState.isServicesLoading || uiState.services.isEmpty()) {
            return@LaunchedEffect
        }
        val serviceStartIndex = 6 // Índice del primer item de servicios cuando ya cargaron filtros.
        snapshotFlow { homeListState.firstVisibleItemIndex }
            .map { firstVisibleIndex -> (firstVisibleIndex - serviceStartIndex).coerceAtLeast(0) }
            .distinctUntilChanged()
            .collect { firstServiceIndex ->
                val urls = uiState.services
                    .drop(firstServiceIndex)
                    .take(5)
                    .mapNotNull { it.imageUrl?.takeIf(String::isNotBlank) }
                prefetchImageUrls(context, imageLoader, urls)
            }
    }

    // Prefetch alrededor de la mascota activa para suavizar el carrusel.
    LaunchedEffect(highlightedPetIndex, uiState.pets) {
        if (uiState.pets.isEmpty()) return@LaunchedEffect
        val urls = uiState.pets
            .drop(highlightedPetIndex.coerceAtLeast(0))
            .take(4)
            .mapNotNull { it.imageUrl?.takeIf(String::isNotBlank) }
        prefetchImageUrls(context, imageLoader, urls)
    }

    // Título dinámico de la sección de servicios
    val nearbyTitle = when {
        uiState.isServicesLoading -> "Buscando servicios cerca de ti…"
        !uiState.hasLocation -> "Servicios disponibles"
        uiState.services.isEmpty() -> "Sin servicios en tu zona"
        uiState.services.size == 1 -> "1 servicio cerca de ti"
        else -> "${uiState.services.size} servicios cerca de ti"
    }

    // ── Contenido principal ───────────────────────────────────────────────────
    LazyColumn(
        state = homeListState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        if (uiState.isLoading) {
            item { HomeContentSkeleton() }
            return@LazyColumn
        }

        item {
            Text(
                text = "Servicios cercanos",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item { MapBannerCard(onClick = { showMapSheet = true }) }

        if (!uiState.isServicesLoading) {
            item {
                LazyRow(
                    state = filterListState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filters, key = { it }) { filter ->
                        val icon = when (filter) {
                            "Clinicas" -> painterResource(R.drawable.ic_stethoscope)
                            "A domicilio" -> painterResource(R.drawable.ic_house)
                            "Spa" -> painterResource(R.drawable.ic_scissors)
                            else -> painterResource(R.drawable.ic_stethoscope)
                        }
                        FilterChipWithIcon(
                            selected = filter == uiState.selectedFilter,
                            onClick = { onFilterSelected(filter) },
                            label = filter,
                            icon = icon
                        )
                    }
                }
            }
        }

        item { SectionTitle(title = "Mascotas en adopcion") }

        item {
            // CarouselState se crea aquí, DENTRO del item, para que
            // rememberCarouselState solo se llame por primera vez cuando
            // pets ya están cargados (itemCount correcto desde el inicio).
            val petsCarouselState = rememberCarouselState { uiState.pets.size }
            HorizontalMultiBrowseCarousel(
                state = petsCarouselState,
                modifier = Modifier.fillMaxWidth(),
                preferredItemWidth = 208.dp,
                itemSpacing = 10.dp,
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 34.dp,
                    top = 6.dp,
                    bottom = 8.dp
                )
            ) { index ->
                val pet = uiState.pets[index]
                val isActive = highlightedPetIndex == index

                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.96f, label = "pet_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.84f, label = "pet_alpha"
                )
                val yOffset by animateDpAsState(
                    targetValue = if (isActive) 0.dp else 6.dp, label = "pet_offset"
                )

                PetCard(
                    pet = pet,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .zIndex(if (isActive) 1f else 0f)
                        .offset(y = yOffset)
                        .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
                        .fillMaxWidth()
                        .clickable {
                            highlightedPetIndex = index
                            onPetClick?.invoke(pet.id)
                        },
                    onClick = onPetClick?.let { { it(pet.id) } },
                    image = pet.imageRes?.let { painterResource(it) },
                    isFavorite = pet.id in favoritePetIds,
                    onFavoriteClick = { onFavoriteToggle(pet.id) }
                )
            }
        }

        item { SectionTitle(title = nearbyTitle) }

        when {
            uiState.isServicesLoading -> {
                items(4) {
                    ServiceCardSkeleton(modifier = Modifier.padding(top = 4.dp))
                }
            }

            uiState.hasLocation && uiState.services.isEmpty() -> {
                item { NearbyEmptyState() }
            }

            else -> {
                items(uiState.services, key = { it.id }) { service ->
                    ServiceCard(
                        service = service,
                        onScheduleClick = { onScheduleService(service) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }

    if (showMapSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMapSheet = false },
            sheetState = mapSheetState
        ) {
            MapScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(560.dp),
                deliveryRequested = false,
                skipPreview = true,
                onBookAppointment = { service ->
                    showMapSheet = false
                    // Forzamos supportsDelivery=false para ir a cita presencial
                    onScheduleService(service.copy(supportsDelivery = false))
                },
                onRequestDelivery = { service ->
                    showMapSheet = false
                    onScheduleService(service)
                }
            )
        }
    }
}

private fun prefetchImageUrls(context: Context, imageLoader: ImageLoader, urls: List<String>) {
    urls.distinct().forEach { url ->
        imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(url)
                .memoryCacheKey(url)
                .diskCacheKey(url)
                .build()
        )
    }
}

 // Estado vacío
 
@Composable
private fun NearbyEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = "No encontramos servicios en tu zona",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Prueba buscar en el mapa para ver más opciones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

 // Banner de acceso al mapa
 
@Composable
private fun MapBannerCard(onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ver clínicas cercanas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Veterinarias, spas y lugares pet friendly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
