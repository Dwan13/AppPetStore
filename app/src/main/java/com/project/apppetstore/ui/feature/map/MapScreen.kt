package com.project.apppetstore.ui.feature.map

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.project.apppetstore.R
import com.project.apppetstore.ui.components.NoPermissionCard
import com.project.apppetstore.ui.components.RequestPermissionCard
import com.project.apppetstore.ui.viewmodels.LocationViewModel
import com.project.apppetstore.ui.viewmodels.SensorViewModel
import kotlin.math.abs

enum class PoiCategory {
    CLINICA,
    SPA,
    DESTACADO
}

data class PoiItem(
    val id: String,
    val name: String,
    val category: PoiCategory,
    val position: LatLng,
    val detail: String
)

private val bogotaCenter = LatLng(4.653332, -74.083652)

private val mockPois = listOf(
    PoiItem(
        id = "poi_1",
        name = "Clinica Vet Central",
        category = PoiCategory.CLINICA,
        position = LatLng(4.653332, -74.083652),
        detail = "Urgencias 24/7"
    ),
    PoiItem(
        id = "poi_2",
        name = "Spa Patitas Felices",
        category = PoiCategory.SPA,
        position = LatLng(4.648625, -74.091640),
        detail = "Bano y peluqueria"
    ),
    PoiItem(
        id = "poi_3",
        name = "Parque Mascotas Norte",
        category = PoiCategory.DESTACADO,
        position = LatLng(4.662711, -74.071426),
        detail = "Punto destacado pet friendly"
    ),
    PoiItem(
        id = "poi_4",
        name = "Clinica Vet Sur",
        category = PoiCategory.CLINICA,
        position = LatLng(4.640045, -74.088929),
        detail = "Consulta y vacunacion"
    )
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    deliveryRequested: Boolean,
    serviceCategory: String? = null,
    locationViewModel: LocationViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationState by locationViewModel.state.collectAsState()
    val sensorState by sensorViewModel.state.collectAsState()
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val showRationale = when (val status = permissionState.status) {
        is PermissionStatus.Denied -> status.shouldShowRationale
        PermissionStatus.Granted -> false
    }

    var selectedCategory by rememberSaveable { mutableStateOf<PoiCategory?>(null) }

    val filteredPois = if (selectedCategory == null) {
        mockPois
    } else {
        mockPois.filter { it.category == selectedCategory }
    }

    val routePoints = locationState.route.map { LatLng(it.latitude, it.longitude) }
    val currentPoint = routePoints.lastOrNull()
    val lightMapStyle = rememberMapStyle(context, R.raw.map_style_light)
    val darkMapStyle = rememberMapStyle(context, R.raw.map_style_dark)

    val activeMapStyle = when {
        sensorState.isLightSensorAvailable && sensorState.isDarkEnvironment -> darkMapStyle
        sensorState.isLightSensorAvailable -> lightMapStyle
        else -> null
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogotaCenter, 12f)
    }

    LaunchedEffect(Unit) {
        locationViewModel.setup(context)
        sensorViewModel.setup(context)
        sensorViewModel.startListening()
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(permissionState.status.isGranted, deliveryRequested) {
        if (permissionState.status.isGranted && deliveryRequested) {
            locationViewModel.startLocationUpdates()
        } else {
            locationViewModel.stopLocationUpdates()
            if (!deliveryRequested) {
                locationViewModel.clearRoute()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationViewModel.stopLocationUpdates()
            sensorViewModel.stopListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CategoryFilters(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        if (!permissionState.status.isGranted) {
            RequestPermissionCard(
                modifier = Modifier.fillMaxWidth(),
                onRequestPermission = { permissionState.launchPermissionRequest() }
            )
        }

        if (!permissionState.status.isGranted && !showRationale) {
            NoPermissionCard(modifier = Modifier.fillMaxWidth())
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = permissionState.status.isGranted,
                mapStyleOptions = activeMapStyle
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = permissionState.status.isGranted
            )
        ) {
            filteredPois.forEach { poi ->
                Marker(
                    state = rememberMarkerState(position = poi.position),
                    title = poi.name,
                    snippet = poi.detail
                )
            }

            if (deliveryRequested && routePoints.size >= 2) {
                Polyline(
                    points = routePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 8f
                )
            }

            val courierMarkerHue = courierHueForCategory(serviceCategory)

            if (deliveryRequested) currentPoint?.let { point ->
                Marker(
                    state = rememberMarkerState(position = point),
                    title = "Domiciliario",
                    snippet = if (locationState.isRecording) {
                        "En recorrido - ${serviceCategory ?: "General"}"
                    } else {
                        "Sin seguimiento"
                    },
                    icon = BitmapDescriptorFactory.defaultMarker(courierMarkerHue)
                )
            }
        }

        if (deliveryRequested) {
            Text(
                text = if (locationState.isRecording) {
                    "Seguimiento del domicilio en tiempo real"
                } else {
                    "Esperando permisos para iniciar el seguimiento"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = buildSensorSummary(sensorState),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun rememberMapStyle(
    context: android.content.Context,
    rawResId: Int
): MapStyleOptions? {
    return androidx.compose.runtime.remember(context, rawResId) {
        runCatching { MapStyleOptions.loadRawResourceStyle(context, rawResId) }.getOrNull()
    }
}

private fun buildSensorSummary(sensorState: com.project.apppetstore.ui.viewmodels.SensorUiState): String {
    val lightText = if (!sensorState.isLightSensorAvailable) {
        "Luz no disponible"
    } else {
        val lux = sensorState.lightLux?.toInt() ?: 0
        val mode = if (sensorState.isDarkEnvironment) "Mapa oscuro" else "Mapa claro"
        "Luz: ${lux} lux ($mode)"
    }

    val accelText = if (!sensorState.isAccelerometerAvailable) {
        "Acelerometro no disponible"
    } else {
        val value = sensorState.accelerometerMagnitude ?: 0f
        val intensity = when {
            value >= 13f -> "Movimiento alto"
            value >= 10f -> "Movimiento medio"
            else -> "Movimiento bajo"
        }
        "$intensity (${value.formatOneDecimal()})"
    }

    val gyroText = if (!sensorState.isGyroscopeAvailable) {
        "Giroscopio no disponible"
    } else {
        val value = sensorState.gyroscopeMagnitude ?: 0f
        val turn = if (abs(value) > 1.2f) "Giro rapido" else "Giro suave"
        "$turn (${value.formatOneDecimal()})"
    }

    return "$lightText | $accelText | $gyroText"
}

private fun Float.formatOneDecimal(): String = String.format("%.1f", this)

@Composable
private fun CategoryFilters(
    selectedCategory: PoiCategory?,
    onCategorySelected: (PoiCategory?) -> Unit
) {
    val options = listOf<PoiCategory?>(null, PoiCategory.CLINICA, PoiCategory.SPA, PoiCategory.DESTACADO)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            val selected = selectedCategory == option
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(option) },
                label = {
                    Text(
                        when (option) {
                            null -> "Todos"
                            PoiCategory.CLINICA -> "Clinicas"
                            PoiCategory.SPA -> "Spas"
                            PoiCategory.DESTACADO -> "Destacados"
                        }
                    )
                }
            )
        }
    }
}

private fun courierHueForCategory(category: String?): Float {
    val normalized = category.orEmpty().lowercase()
    return when {
        normalized.contains("veter") -> BitmapDescriptorFactory.HUE_RED
        normalized.contains("spa") || normalized.contains("pelu") -> BitmapDescriptorFactory.HUE_VIOLET
        normalized.contains("destac") -> BitmapDescriptorFactory.HUE_ORANGE
        else -> BitmapDescriptorFactory.HUE_AZURE
    }
}
