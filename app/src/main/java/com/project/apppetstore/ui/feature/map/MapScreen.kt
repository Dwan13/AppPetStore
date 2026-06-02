package com.project.apppetstore.ui.feature.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.project.apppetstore.R
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.ui.components.RequestPermissionCard
import com.project.apppetstore.ui.feature.services.ServicesViewModel
import com.project.apppetstore.ui.viewmodels.LocationViewModel
import com.project.apppetstore.ui.viewmodels.SensorViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

 // Data / Enums
 
enum class PoiCategory { CLINICA, SPA, DOMICILIO }

data class PoiItem(
    val id: String,
    val name: String,
    val category: PoiCategory,
    val position: LatLng,
    val detail: String,
    /** Link to the real Service from the database (null for fallback entries). */
    val service: Service? = null
)

private val bogotaCenter = LatLng(4.653332, -74.083652)

/**
 * Fallback POIs shown while the service repository is still loading, or
 * when no service has valid coordinates.
 */
private val fallbackPois = listOf(
    PoiItem(
        "c01",
        "Clínica Vet Central",
        PoiCategory.CLINICA,
        LatLng(4.6420, -74.0588),
        "Urgencias 24/7"
    ),
    PoiItem(
        "c02",
        "Fauna Clínica Usaquén",
        PoiCategory.CLINICA,
        LatLng(4.6897, -74.0425),
        "Consulta y vacunación"
    ),
    PoiItem(
        "c03",
        "Vetpet Suba Colina",
        PoiCategory.CLINICA,
        LatLng(4.7340, -74.0916),
        "Laboratorio clínico"
    ),
    PoiItem(
        "c04",
        "Hospital Veterinario Sur",
        PoiCategory.CLINICA,
        LatLng(4.6291, -74.1559),
        "Cirugía y hospitalización"
    ),
    PoiItem(
        "s01",
        "Spa Patitas Felices",
        PoiCategory.SPA,
        LatLng(4.6673, -74.0836),
        "Baño y peluquería canina"
    ),
    PoiItem(
        "s02",
        "Pet Grooming Chicó",
        PoiCategory.SPA,
        LatLng(4.6739, -74.0432),
        "Grooming premium"
    ),
    PoiItem(
        "d01",
        "Dr. Carlos Ruiz",
        PoiCategory.DOMICILIO,
        LatLng(4.6351, -74.0636),
        "Veterinario a domicilio"
    ),
    PoiItem(
        "d02",
        "Diego Reina — Cuidador",
        PoiCategory.DOMICILIO,
        LatLng(4.6476, -74.0810),
        "Cuidado de mascotas en tu hogar"
    )
)

// Origen por defecto (fallback cuando el servicio no tiene coordenadas)
private val deliveryOrigin = LatLng(4.6533, -74.0837)

// Destino por defecto si el usuario no seleccionó punto en el mapa
private val defaultDestination = LatLng(4.6619, -74.0818)

/**
 * Construye una ruta de [steps+1] waypoints equiespaciados en línea recta
 * entre [origin] y [destination].
 */
private fun buildSimulationRoute(
    origin: LatLng,
    destination: LatLng,
    steps: Int = 7
): List<LatLng> =
    (0..steps).map { i ->
        val t = i.toDouble() / steps
        LatLng(
            origin.latitude + (destination.latitude - origin.latitude) * t,
            origin.longitude + (destination.longitude - origin.longitude) * t
        )
    }

/** ETA aproximado en segundos según distancia (25 km/h promedio en ciudad). */
private fun roughEtaSeconds(origin: LatLng, destination: LatLng): Int {
    val dlat = destination.latitude - origin.latitude
    val dlng = destination.longitude - origin.longitude
    val distKm = Math.sqrt(dlat * dlat + dlng * dlng) * 111.0
    return ((distKm / 25.0) * 3600).toInt().coerceIn(45, 600)
}

 // Entry point — preview → mapa
 
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    deliveryRequested: Boolean,
    serviceCategory: String? = null,
    skipPreview: Boolean = false,
    destLat: Double? = null,
    destLng: Double? = null,
    originLat: Double? = null,
    originLng: Double? = null,
    /** Called when the user taps "Solicitar cita" on a map marker. */
    onBookAppointment: ((Service) -> Unit)? = null,
    /** Called when the user taps "A domicilio" on a map marker. */
    onRequestDelivery: ((Service) -> Unit)? = null,
    locationViewModel: LocationViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel()
) {
    // Si viene del flujo de delivery o desde el bottom sheet, saltamos el preview.
    var showMap by rememberSaveable { mutableStateOf(deliveryRequested || skipPreview) }

    if (!showMap) {
        MapPreviewScreen(
            modifier = modifier,
            onEnterMap = { showMap = true }
        )
        return
    }

    MapContent(
        modifier = modifier,
        deliveryRequested = deliveryRequested,
        serviceCategory = serviceCategory,
        destLat = destLat,
        destLng = destLng,
        originLat = originLat,
        originLng = originLng,
        onBookAppointment = onBookAppointment,
        onRequestDelivery = onRequestDelivery,
        locationViewModel = locationViewModel,
        sensorViewModel = sensorViewModel
    )
}

 // Pantalla de preview / bienvenida
 
@Composable
private fun MapPreviewScreen(
    modifier: Modifier = Modifier,
    onEnterMap: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // ── Hero icon ────────────────────────────────────────────────────────
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(136.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(68.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Título ───────────────────────────────────────────────────────────
        Text(
            text = "Clínicas y servicios\ncercanos a ti",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Descripción ──────────────────────────────────────────────────────
        Text(
            text = "Encuentra veterinarias, spas y servicios a domicilio cerca de ti.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Chips de categorías ──────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MapFeatureChip(icon = Icons.Rounded.LocalHospital, label = "Clínicas")
            MapFeatureChip(icon = Icons.Rounded.Spa, label = "Spas")
            MapFeatureChip(icon = Icons.Rounded.DeliveryDining, label = "Domicilio")
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── CTA principal ────────────────────────────────────────────────────
        Button(
            onClick = onEnterMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = Icons.Rounded.NearMe,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ver clínicas cercanas",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Aviso de permisos ────────────────────────────────────────────────
        Text(
            text = "Se solicitará permiso de ubicación al continuar",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MapFeatureChip(icon: ImageVector, label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

 // Contenido completo del mapa
 
@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapContent(
    modifier: Modifier = Modifier,
    deliveryRequested: Boolean,
    serviceCategory: String? = null,
    destLat: Double? = null,
    destLng: Double? = null,
    originLat: Double? = null,
    originLng: Double? = null,
    onBookAppointment: ((Service) -> Unit)? = null,
    onRequestDelivery: ((Service) -> Unit)? = null,
    locationViewModel: LocationViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel(),
    servicesViewModel: ServicesViewModel = viewModel()
) {
    val context = LocalContext.current
    val sensorState by sensorViewModel.state.collectAsState()
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val showRationale = when (val status = permissionState.status) {
        is PermissionStatus.Denied -> status.shouldShowRationale
        PermissionStatus.Granted -> false
    }
    val isExplorationWithoutPermission = !deliveryRequested && !permissionState.status.isGranted
    val requestLocationAccess: () -> Unit = {
        when (val status = permissionState.status) {
            PermissionStatus.Granted -> Unit
            is PermissionStatus.Denied -> {
                if (status.shouldShowRationale) permissionState.launchPermissionRequest()
                else openAppPermissionSettings(context)
            }
        }
    }

    var selectedCategory by rememberSaveable { mutableStateOf<PoiCategory?>(null) }

    // Servicio seleccionado al tocar un marcador en el mapa de exploración.
    var selectedService by remember { mutableStateOf<Service?>(null) }

    // ── Servicios reales desde el repositorio ────────────────────────────────
    val services = servicesViewModel.uiState.services

    // Convertir cada Service con coordenadas válidas en un PoiItem del mapa.
    val servicePois = remember(services) {
        services.mapNotNull { svc ->
            if (svc.lat == 0.0 && svc.lng == 0.0) null
            else PoiItem(
                id = svc.id,
                name = svc.name,
                category = when (svc.category) {
                    "Clinicas" -> PoiCategory.CLINICA
                    "Spa" -> PoiCategory.SPA
                    "A domicilio" -> PoiCategory.DOMICILIO
                    else -> PoiCategory.CLINICA
                },
                position = LatLng(svc.lat, svc.lng),
                detail = svc.description,
                service = svc
            )
        }
    }

    // Ubicación capturada en el momento en que el usuario abre el mapa de exploración.
    var userSearchLocation by remember { mutableStateOf<Location?>(null) }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted && !deliveryRequested) {
            fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc -> if (loc != null) userSearchLocation = loc }
                .addOnFailureListener {
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> if (loc != null) userSearchLocation = loc }
                }
        }
    }

    // Filtra por categoría y ordena por distancia.
    // Usa los servicios reales si hay alguno con coordenadas; si no, el fallback.
    val filteredPois: List<PoiItem> = remember(
        selectedCategory,
        userSearchLocation,
        servicePois,
        isExplorationWithoutPermission
    ) {
        if (isExplorationWithoutPermission) return@remember emptyList()
        val allPois = if (servicePois.isNotEmpty()) servicePois else fallbackPois
        val base = if (selectedCategory == null) allPois
        else allPois.filter { it.category == selectedCategory }
        val searchLoc = userSearchLocation
        if (searchLoc != null) {
            val results = FloatArray(1)
            base.sortedBy { poi ->
                Location.distanceBetween(
                    searchLoc.latitude, searchLoc.longitude,
                    poi.position.latitude, poi.position.longitude,
                    results
                )
                results[0]
            }
        } else base
    }

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

    var isMapLoading by remember { mutableStateOf(true) }

    // ── Origen del servicio ──────────────────────────────────────────────────
    val origin = remember(originLat, originLng) {
        if (originLat != null && originLng != null) LatLng(originLat, originLng)
        else deliveryOrigin
    }

    // ── Destino elegido en el formulario (o fallback) ─────────────────────────
    val destination = remember(destLat, destLng) {
        if (destLat != null && destLng != null) LatLng(destLat, destLng)
        else defaultDestination
    }

    var activeRoute by remember { mutableStateOf(buildSimulationRoute(origin, destination)) }
    var isRouteLoading by remember { mutableStateOf(deliveryRequested) }

    // ── Estado de la simulación ──────────────────────────────────────────────
    val courierLat = remember { Animatable(origin.latitude.toFloat()) }
    val courierLng = remember { Animatable(origin.longitude.toFloat()) }
    var simStep by remember { mutableIntStateOf(0) }
    var simArrived by remember { mutableStateOf(false) }

    var initialEtaSeconds by remember { mutableIntStateOf(roughEtaSeconds(origin, destination)) }
    var simStepMs by remember { mutableIntStateOf(2_000) }

    val segments = (activeRoute.size - 1).coerceAtLeast(1)
    val progress = simStep.toFloat() / segments.toFloat()
    val displayEtaSeconds = (initialEtaSeconds * (1f - progress)).toInt()

    val courierPos = LatLng(courierLat.value.toDouble(), courierLng.value.toDouble())

    val traveledPath: List<LatLng> = activeRoute.take(simStep + 1) + listOf(courierPos)
    val remainPath: List<LatLng> = listOf(courierPos) + activeRoute.drop(simStep + 1)

    val courierMarkerState = remember { MarkerState(position = origin) }
    SideEffect { courierMarkerState.position = courierPos }

    // ── 1. Fetch de la ruta real (Directions API) ─────────────────────────────
    LaunchedEffect(destination) {
        if (!deliveryRequested) return@LaunchedEffect

        val apiKey = runCatching {
            context.packageManager
                .getApplicationInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_META_DATA
                )
                .metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        }.getOrElse { "" }

        val result = com.project.apppetstore.data.network.DirectionsService
            .getRoute(origin, destination, apiKey)

        if (result.waypoints.size >= 2) {
            activeRoute = result.waypoints
            if (result.durationSeconds > 0) {
                initialEtaSeconds = result.durationSeconds.coerceAtLeast(30)
                simStepMs = (result.durationSeconds * 1_000L /
                        (result.waypoints.size.coerceAtLeast(1) * 6L))
                    .coerceIn(800L, 5_000L).toInt()
            }
        }
        isRouteLoading = false
    }

    // ── 2. Animación del domiciliario ─────────────────────────────────────────
    LaunchedEffect(deliveryRequested, isRouteLoading) {
        if (!deliveryRequested || isRouteLoading) return@LaunchedEffect

        val route = activeRoute
        val msStep = simStepMs

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(route.first(), 16f),
            durationMs = 1_200
        )
        delay(500L)

        for (i in 1 until route.size) {
            val target = route[i]
            coroutineScope {
                launch {
                    courierLat.animateTo(
                        targetValue = target.latitude.toFloat(),
                        animationSpec = tween(msStep, easing = LinearEasing)
                    )
                }
                launch {
                    courierLng.animateTo(
                        targetValue = target.longitude.toFloat(),
                        animationSpec = tween(msStep, easing = LinearEasing)
                    )
                }
            }
            simStep = i
            if (i == route.lastIndex) simArrived = true
        }
    }

    // ── 3. Cámara sigue al domiciliario ──────────────────────────────────────
    LaunchedEffect(simStep) {
        if (!deliveryRequested || activeRoute.isEmpty()) return@LaunchedEffect
        val target = activeRoute.getOrNull(simStep) ?: return@LaunchedEffect
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(target, 16f),
            durationMs = simStepMs
        )
    }

    // ── Setup de sensores y ubicación ────────────────────────────────────────
    LaunchedEffect(Unit) {
        locationViewModel.setup(context)
        sensorViewModel.setup(context)
        sensorViewModel.startListening()
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(permissionState.status.isGranted, deliveryRequested) {
        if (permissionState.status.isGranted && !deliveryRequested) {
            locationViewModel.startLocationUpdates()
        } else {
            locationViewModel.stopLocationUpdates()
            locationViewModel.clearRoute()
            if (!deliveryRequested) selectedService = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationViewModel.stopLocationUpdates()
            sensorViewModel.stopListening()
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        if (isMapLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        if (!deliveryRequested && !isExplorationWithoutPermission) {
            CategoryFilters(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    selectedService = null   // deselecciona al cambiar filtro
                }
            )
        }

        if (isExplorationWithoutPermission) {
            RequestPermissionCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Activa tu ubicacion para usar el mapa",
                message = if (showRationale) {
                    "Necesitamos el permiso para mostrar clinicas y ordenar resultados cerca de ti."
                } else {
                    "El permiso fue bloqueado. Abre Ajustes para habilitar la ubicacion y ver servicios cercanos."
                },
                actionLabel = if (showRationale) "Solicitar permisos" else "Abrir ajustes",
                onRequestPermission = requestLocationAccess
            )
        }

        if (isExplorationWithoutPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Sin permiso no mostramos ubicaciones en el mapa para proteger tu contexto de busqueda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }

        // ── Google Map ───────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { isMapLoading = false },
            onMapClick = { selectedService = null },   // deselecciona al tocar el mapa
            properties = MapProperties(
                isMyLocationEnabled = permissionState.status.isGranted && !deliveryRequested,
                mapStyleOptions = activeMapStyle
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = permissionState.status.isGranted && !deliveryRequested
            )
        ) {
            if (deliveryRequested) {
                // ── Modo simulación de domicilio ─────────────────────────────

                MarkerComposable(
                    keys = arrayOf("origin"),
                    state = rememberMarkerState(position = activeRoute.first()),
                    title = serviceCategory ?: "Punto de recogida",
                    snippet = "Origen del domicilio"
                ) {
                    DeliveryMarkerPin(
                        icon = Icons.Rounded.LocalHospital,
                        backgroundColor = Color(0xFF2E7D32)
                    )
                }

                MarkerComposable(
                    keys = arrayOf("destination"),
                    state = rememberMarkerState(position = activeRoute.last()),
                    title = "Tu dirección",
                    snippet = "Destino de entrega"
                ) {
                    DeliveryMarkerPin(
                        icon = Icons.Rounded.Pets,
                        backgroundColor = Color(0xFFE65100)
                    )
                }

                if (remainPath.size >= 2) {
                    Polyline(
                        points = remainPath,
                        color = Color(0xFFBDBDBD),
                        width = 6f
                    )
                }

                if (traveledPath.size >= 2) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Polyline(
                        points = traveledPath,
                        color = primaryColor,
                        width = 9f
                    )
                }

                MarkerComposable(
                    keys = arrayOf("courier"),
                    state = courierMarkerState,
                    title = "Carlos M. · Domiciliario",
                    snippet = serviceCategory ?: "Domicilio en camino"
                ) {
                    DeliveryMarkerPin(
                        icon = Icons.Rounded.TwoWheeler,
                        backgroundColor = Color(0xFF1565C0),
                        pinSize = 46
                    )
                }

            } else {
                // ── Modo exploración de puntos de interés ────────────────────
                filteredPois.forEach { poi ->

                    val distanceSnippet = if (userSearchLocation != null) {
                        val r = FloatArray(1)
                        Location.distanceBetween(
                            userSearchLocation!!.latitude, userSearchLocation!!.longitude,
                            poi.position.latitude, poi.position.longitude,
                            r
                        )
                        val distKm = r[0] / 1_000f
                        val distStr = if (distKm < 1f) "${r[0].toInt()} m"
                        else "${"%.1f".format(distKm)} km"
                        "${poi.detail} · $distStr"
                    } else poi.detail

                    val (poiIcon, poiColor) = when (poi.category) {
                        PoiCategory.CLINICA -> Icons.Rounded.LocalHospital to Color(0xFF1B5E20)
                        PoiCategory.SPA -> Icons.Rounded.Spa to Color(0xFF6A1B9A)
                        PoiCategory.DOMICILIO -> Icons.Rounded.DeliveryDining to Color(0xFFE65100)
                    }

                    key(poi.id) {
                        MarkerComposable(
                            keys = arrayOf(poi.id),
                            state = rememberMarkerState(position = poi.position),
                            title = poi.name,
                            snippet = distanceSnippet,
                            onClick = { _ ->
                                selectedService = poi.service
                                true   // consumimos el click: no mostrar info window por defecto
                            }
                        ) {
                            DeliveryMarkerPin(
                                icon = poiIcon,
                                backgroundColor = poiColor,
                                pinSize = 36
                            )
                        }
                    }
                }
            }
        }

        // ── Panel inferior ───────────────────────────────────────────────────
        if (deliveryRequested) {
            if (isRouteLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "Calculando ruta ...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                DeliveryStatusCard(
                    serviceCategory = serviceCategory,
                    simStep = simStep,
                    totalSteps = activeRoute.size,
                    etaSeconds = displayEtaSeconds,
                    simArrived = simArrived
                )
            }
        } else {
            // Modo exploración: muestra la tarjeta de acción cuando hay un servicio seleccionado.
            val svc = selectedService
            if (svc != null && !isExplorationWithoutPermission) {
                ServiceActionCard(
                    service = svc,
                    onDismiss = { selectedService = null },
                    onBookAppointment = if (onBookAppointment != null) {
                        { onBookAppointment(svc) }
                    } else null,
                    onRequestDelivery = if (svc.supportsDelivery && onRequestDelivery != null) {
                        { onRequestDelivery(svc) }
                    } else null
                )
            }
        }
    }
}

 // Tarjeta de acción al seleccionar un servicio del mapa (modo exploración)
 
@Composable
private fun ServiceActionCard(
    service: Service,
    onDismiss: () -> Unit,
    onBookAppointment: (() -> Unit)? = null,
    onRequestDelivery: (() -> Unit)? = null
) {
    val (catIcon, catColor) = when (service.category) {
        "Clinicas" -> Icons.Rounded.LocalHospital to Color(0xFF1B5E20)
        "Spa" -> Icons.Rounded.Spa to Color(0xFF6A1B9A)
        "A domicilio" -> Icons.Rounded.DeliveryDining to Color(0xFFE65100)
        else -> Icons.Rounded.LocalHospital to Color(0xFF1B5E20)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── Encabezado: icono + nombre + botón cerrar ────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = catColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Botones de acción ────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // "Solicitar cita" — siempre visible si hay callback
                if (onBookAppointment != null) {
                    OutlinedButton(
                        onClick = onBookAppointment,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Solicitar cita",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // "A domicilio" — solo si el servicio lo soporta y hay callback
                if (onRequestDelivery != null) {
                    Button(
                        onClick = onRequestDelivery,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeliveryDining,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "A domicilio",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

 // Tarjeta de estado del domicilio
 
@Composable
private fun DeliveryStatusCard(
    serviceCategory: String?,
    simStep: Int,
    totalSteps: Int,
    etaSeconds: Int,
    simArrived: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (simArrived)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            if (simArrived) {
                // ── Domicilio entregado ──────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                    Column {
                        Text(
                            text = "¡Tu domicilio llegó!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Carlos M. está en la puerta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            } else {
                // ── En tránsito ──────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Carlos M.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeliveryDining,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Moto  •  ZXC-890",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val min = etaSeconds / 60
                        val sec = etaSeconds % 60
                        Text(
                            text = "%02d:%02d".format(min, sec),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "restante",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val segments = (totalSteps - 1).coerceAtLeast(1)
                val progress = simStep.toFloat() / segments.toFloat()

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${serviceCategory ?: "Domicilio"} en camino  •  ${(progress * 100).toInt()}% del trayecto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

 // Helpers
 
@Composable
private fun rememberMapStyle(context: android.content.Context, rawResId: Int): MapStyleOptions? {
    return remember(context, rawResId) {
        runCatching { MapStyleOptions.loadRawResourceStyle(context, rawResId) }.getOrNull()
    }
}

private fun openAppPermissionSettings(context: android.content.Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

@Composable
private fun CategoryFilters(
    selectedCategory: PoiCategory?,
    onCategorySelected: (PoiCategory?) -> Unit
) {
    val options =
        listOf<PoiCategory?>(null, PoiCategory.CLINICA, PoiCategory.SPA, PoiCategory.DOMICILIO)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            FilterChip(
                selected = selectedCategory == option,
                onClick = { onCategorySelected(option) },
                label = {
                    Text(
                        when (option) {
                            null -> "Todos"
                            PoiCategory.CLINICA -> "Clínicas"
                            PoiCategory.SPA -> "Spas"
                            PoiCategory.DOMICILIO -> "A domicilio"
                        }
                    )
                }
            )
        }
    }
}

 // Marcador personalizado
 
@Composable
private fun DeliveryMarkerPin(
    icon: ImageVector,
    backgroundColor: Color,
    tint: Color = Color.White,
    pinSize: Int = 40
) {
    val sizeDp = pinSize.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp + 4.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(sizeDp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(sizeDp * 0.55f)
                )
            }
        }

        Canvas(modifier = Modifier.size(14.dp, 8.dp)) {
            val cw = size.width
            val ch = size.height
            drawPath(
                path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(cw, 0f)
                    lineTo(cw / 2f, ch)
                    close()
                },
                color = backgroundColor
            )
        }
    }
}
