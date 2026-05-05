package com.project.apppetstore.ui.feature.home

import android.Manifest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.R
import com.project.apppetstore.ui.components.FilterChipWithIcon
import com.project.apppetstore.ui.components.NoPermissionCard
import com.project.apppetstore.ui.components.PetCard
import com.project.apppetstore.ui.components.RequestPermissionCard
import com.project.apppetstore.ui.components.SectionTitle
import com.project.apppetstore.ui.components.ServiceCard

private val homeBogotaCenter = LatLng(4.653332, -74.083652)
private val homeMapPois = listOf(
    LatLng(4.653332, -74.083652),
    LatLng(4.648625, -74.091640),
    LatLng(4.662711, -74.071426),
    LatLng(4.640045, -74.088929)
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onPetClick: ((petId: String) -> Unit)? = null,
    onFilterSelected: (String) -> Unit = {},
    onScheduleService: (Service) -> Unit = {}
) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var isMapExpanded by rememberSaveable { mutableStateOf(false) }
    var highlightedPetIndex by rememberSaveable { mutableStateOf(0) }
    val petsCarouselState = rememberCarouselState { uiState.pets.size }
    val mapHeight = if (isMapExpanded) 420.dp else 180.dp

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(homeBogotaCenter, 12f)
    }

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Servicios cercanos",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .animateContentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(400f, 400f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isMapExpanded = !isMapExpanded }
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = permissionState.status.isGranted
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = permissionState.status.isGranted
                    )
                ) {
                    homeMapPois.forEachIndexed { index, poi ->
                        Marker(
                            state = rememberMarkerState(position = poi),
                            title = "Punto ${index + 1}"
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { isMapExpanded = !isMapExpanded },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (isMapExpanded) Icons.Outlined.CloseFullscreen else Icons.Outlined.OpenInFull,
                        contentDescription = null
                    )
                    Text(
                        text = if (isMapExpanded) "Reducir" else "Ampliar",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        item {
            val showRationale = when (val status = permissionState.status) {
                is PermissionStatus.Denied -> status.shouldShowRationale
                PermissionStatus.Granted -> false
            }

            if (!permissionState.status.isGranted) {
                RequestPermissionCard(
                    modifier = Modifier.fillMaxWidth(),
                    onRequestPermission = { permissionState.launchPermissionRequest() }
                )

                if (!showRationale) {
                    NoPermissionCard(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filters) { filter ->
                    val icon = when (filter) {
                        "Clinicas" -> androidx.compose.ui.res.painterResource(R.drawable.ic_stethoscope)
                        "A domicilio" -> androidx.compose.ui.res.painterResource(R.drawable.ic_house)
                        "Spa" -> androidx.compose.ui.res.painterResource(R.drawable.ic_scissors)
                        else -> androidx.compose.ui.res.painterResource(R.drawable.ic_stethoscope)
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

        item { SectionTitle(title = "Mascotas en adopcion") }
        item {
            HorizontalMultiBrowseCarousel(
                state = petsCarouselState,
                modifier = Modifier.fillMaxWidth(),
                preferredItemWidth = 208.dp,
                itemSpacing = 10.dp,
                // Padding asimetrico: ancla visual a la izquierda y evita que las cards laterales se perciban recortadas.
                contentPadding = PaddingValues(start = 10.dp, end = 34.dp)
            ) { index ->
                val pet = uiState.pets[index]
                val isActive = highlightedPetIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.96f,
                    label = "home_pet_card_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.84f,
                    label = "home_pet_card_alpha"
                )
                val yOffset by animateDpAsState(
                    targetValue = if (isActive) 0.dp else 6.dp,
                    label = "home_pet_card_offset"
                )
                PetCard(
                    pet = pet,
                    modifier = Modifier
                        .zIndex(if (isActive) 1f else 0f)
                        .offset(y = yOffset)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .fillMaxWidth()
                        .clickable {
                            highlightedPetIndex = index
                            onPetClick?.invoke(pet.id)
                        },
                    onClick = onPetClick?.let { { it(pet.id) } },
                    image = pet.imageRes?.let { androidx.compose.ui.res.painterResource(it) }
                )
            }
        }

        item { SectionTitle(title = "Servicios a domicilio") }
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
