package com.project.apppetstore.ui.feature.delivery

import android.Manifest
import android.annotation.SuppressLint
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScheduleScreen(
    serviceId       : String?,
    serviceCategory : String?,
    onConfirm       : (destLat: Double?, destLng: Double?) -> Unit,
    modifier        : Modifier = Modifier
) {
    // ── Estado ────────────────────────────────────────────────────────────────
    var addressText     by rememberSaveable { mutableStateOf("") }
    var mapPickedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showMapPicker   by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker  by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    var showTimePicker  by remember { mutableStateOf(false) }
    var timeConfirmed   by rememberSaveable { mutableStateOf(false) }

    // Derivados
    val hasAddress  = addressText.isNotBlank() || mapPickedLatLng != null
    val hasDate     = datePickerState.selectedDateMillis != null
    val hasTime     = timeConfirmed
    val isFormValid = hasAddress && hasDate && hasTime

    val formattedDate = datePickerState.selectedDateMillis?.let { millis ->
        SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale("es"))
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))
            .replaceFirstChar { it.uppercase() }
    } ?: ""

    val formattedTime = if (timeConfirmed)
        "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
    else ""

    // ── Contenido principal ───────────────────────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {

        // ── Encabezado ────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text       = "Agendar domicilio",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (serviceCategory != null || serviceId != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    serviceCategory?.let { cat ->
                        FilterChip(
                            selected = true,
                            onClick  = {},
                            label    = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector        = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier           = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                    serviceId?.let { id ->
                        Text(
                            text  = "· $id",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // ── Sección 1: Dirección ──────────────────────────────────────────────
        FormSection(title = "Dirección de entrega", icon = Icons.Rounded.LocationOn) {

            OutlinedTextField(
                value         = addressText,
                onValueChange = {
                    addressText = it
                    if (it.isNotEmpty()) mapPickedLatLng = null
                },
                placeholder  = { Text("Escribe la dirección o selecciona en el mapa") },
                modifier     = Modifier.fillMaxWidth(),
                shape        = RoundedCornerShape(12.dp),
                singleLine   = true,
                trailingIcon = {
                    if (addressText.isNotEmpty()) {
                        IconButton(onClick = { addressText = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Borrar")
                        }
                    }
                }
            )

            mapPickedLatLng?.let { latlng ->
                Spacer(modifier = Modifier.height(2.dp))
                Card(
                    shape  = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = "Ubicación en el mapa",
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text  = "%.5f, %.5f".format(latlng.latitude, latlng.longitude),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick  = { mapPickedLatLng = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Close,
                                contentDescription = "Quitar ubicación",
                                modifier           = Modifier.size(14.dp),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick  = { showMapPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Map,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar en el mapa")
            }
        }

        // ── Sección 2: Fecha ──────────────────────────────────────────────────
        FormSection(title = "Fecha de entrega", icon = Icons.Rounded.CalendarMonth) {
            val dateInteraction = remember { MutableInteractionSource() }
            val isDatePressed   by dateInteraction.collectIsPressedAsState()
            LaunchedEffect(isDatePressed) { if (isDatePressed) showDatePicker = true }

            OutlinedTextField(
                value             = formattedDate,
                onValueChange     = {},
                readOnly          = true,
                interactionSource = dateInteraction,
                placeholder       = { Text("Selecciona una fecha") },
                modifier          = Modifier.fillMaxWidth(),
                shape             = RoundedCornerShape(12.dp),
                trailingIcon      = {
                    FilledTonalIconButton(
                        onClick  = { showDatePicker = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Abrir calendario")
                    }
                }
            )
        }

        // ── Sección 3: Hora ───────────────────────────────────────────────────
        FormSection(title = "Hora de entrega", icon = Icons.Rounded.AccessTime) {
            val timeInteraction = remember { MutableInteractionSource() }
            val isTimePressed   by timeInteraction.collectIsPressedAsState()
            LaunchedEffect(isTimePressed) { if (isTimePressed) showTimePicker = true }

            OutlinedTextField(
                value             = formattedTime,
                onValueChange     = {},
                readOnly          = true,
                interactionSource = timeInteraction,
                placeholder       = { Text("Selecciona una hora") },
                modifier          = Modifier.fillMaxWidth(),
                shape             = RoundedCornerShape(12.dp),
                trailingIcon      = {
                    FilledTonalIconButton(
                        onClick  = { showTimePicker = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Rounded.AccessTime, contentDescription = "Abrir reloj")
                    }
                }
            )
        }

        // ── Resumen antes de confirmar ────────────────────────────────────────
        if (isFormValid) {
            ScheduleSummaryCard(
                address  = mapPickedLatLng
                    ?.let { "Mapa: %.4f, %.4f".format(it.latitude, it.longitude) }
                    ?: addressText,
                date     = formattedDate,
                time     = formattedTime,
                category = serviceCategory
            )
        }

        // ── Botón de confirmación ─────────────────────────────────────────────
        Button(
            onClick  = { onConfirm(mapPickedLatLng?.latitude, mapPickedLatLng?.longitude) },
            enabled  = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirmar agendamiento", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // ── DatePickerDialog ──────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── TimePickerDialog ──────────────────────────────────────────────────────
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title   = { Text("Seleccionar hora") },
            text    = {
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    timeConfirmed  = true
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        )
    }

    // ── Selector de ubicación pantalla completa ───────────────────────────────
    if (showMapPicker) {
        MapPickerFullScreen(
            onDismiss          = { showMapPicker = false },
            onLocationSelected = { latLng ->
                mapPickedLatLng = latLng
                addressText     = ""
                showMapPicker   = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sección de formulario
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormSection(
    title   : String,
    icon    : ImageVector,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta de resumen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScheduleSummaryCard(
    address  : String,
    date     : String,
    time     : String,
    category : String?
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text       = "Resumen del agendamiento",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSecondaryContainer
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
            SummaryRow(label = "Servicio",  value = category ?: "—")
            SummaryRow(label = "Dirección", value = address)
            SummaryRow(label = "Fecha",     value = date)
            SummaryRow(label = "Hora",      value = time)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier   = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Selector de dirección en pantalla completa
//
// Usa Dialog en lugar de ModalBottomSheet → sin conflicto de gestos con el mapa.
// Usa requestLocationUpdates con setWaitForAccurateLocation(true) para forzar
// un fix del chip GPS real, evitando que el sistema devuelva la ubicación de
// red (Wi-Fi/torres) que puede ser inexacta o pertenecer a otro país.
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapPickerFullScreen(
    onDismiss          : () -> Unit,
    onLocationSelected : (LatLng) -> Unit
) {
    val context            = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedClient        = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estados del mapa
    var pendingLatLng  by remember { mutableStateOf<LatLng?>(null) }
    var jumpToLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLocating     by remember { mutableStateOf(false) }
    val markerState    = remember { MarkerState() }

    val bogotaCenter = LatLng(4.6533, -74.0836)
    val cameraPos    = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogotaCenter, 13f)
    }

    // ── Callback GPS — creado una sola vez, reutilizable ─────────────────────
    // Se capturan los MutableState directamente; su setter es seguro desde
    // el hilo principal (Looper.getMainLooper() garantiza el contexto).
    val gpsCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                // Cancelar futuras actualizaciones (solo necesitamos un fix)
                fusedClient.removeLocationUpdates(this)
                isLocating = false
                result.lastLocation?.let { loc ->
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    pendingLatLng        = latLng
                    markerState.position = latLng
                    jumpToLocation       = latLng
                }
            }
        }
    }

    // Limpieza al salir del composable (por si el usuario cierra antes del fix)
    DisposableEffect(Unit) {
        onDispose { fusedClient.removeLocationUpdates(gpsCallback) }
    }

    // ── Función para solicitar ubicación GPS real ─────────────────────────────
    // LocationRequest con setWaitForAccurateLocation(true): el sistema espera
    // un fix del chip GPS en lugar de devolver inmediatamente la posición de red.
    // setMaxUpdates(1): una sola lectura, sin tracking continuo.
    fun requestGpsLocation() {
        if (isLocating) return
        isLocating = true

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMaxUpdates(1)
            .setMinUpdateIntervalMillis(1_000L)
            .setWaitForAccurateLocation(true)
            .build()

        fusedClient.requestLocationUpdates(request, gpsCallback, Looper.getMainLooper())
    }

    // ── Animar cámara hacia el punto solicitado ───────────────────────────────
    LaunchedEffect(jumpToLocation) {
        val loc = jumpToLocation ?: return@LaunchedEffect
        cameraPos.animate(
            update     = CameraUpdateFactory.newLatLngZoom(loc, 16f),
            durationMs = 700
        )
    }

    // ── Auto-centrar al abrir el picker si ya hay permiso ─────────────────────
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) requestGpsLocation()
    }

    // ── Dialog pantalla completa — sin drag, sin conflicto con el mapa ────────
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Barra superior ────────────────────────────────────────────
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector        = Icons.Rounded.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = "Seleccionar dirección",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text  = if (isLocating) "Obteniendo ubicación GPS…"
                                    else "Toca el mapa o usa el botón GPS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // ── Mapa (ocupa todo el espacio disponible) ───────────────────
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier            = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPos,
                        onMapClick          = { latLng ->
                            // Tap manual cancela el GPS en curso
                            if (isLocating) {
                                fusedClient.removeLocationUpdates(gpsCallback)
                                isLocating = false
                            }
                            pendingLatLng        = latLng
                            markerState.position = latLng
                        },
                        properties = MapProperties(
                            isMyLocationEnabled = locationPermission.status.isGranted
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled     = true,
                            myLocationButtonEnabled = false
                        )
                    ) {
                        pendingLatLng?.let {
                            Marker(
                                state   = markerState,
                                title   = "Dirección de entrega",
                                snippet = "%.4f, %.4f".format(it.latitude, it.longitude)
                            )
                        }
                    }

                    // ── Botón GPS (esquina superior derecha) ──────────────────
                    SmallFloatingActionButton(
                        onClick        = {
                            if (locationPermission.status.isGranted) {
                                requestGpsLocation()
                            } else {
                                locationPermission.launchPermissionRequest()
                            }
                        },
                        modifier       = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector        = Icons.Rounded.MyLocation,
                                contentDescription = "Usar mi ubicación GPS",
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── Barra de coordenadas ──────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        Icon(
                            imageVector = if (pendingLatLng != null) Icons.Rounded.LocationOn
                                          else Icons.Rounded.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = when {
                                pendingLatLng != null -> MaterialTheme.colorScheme.primary
                                else                  -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Text(
                        text = pendingLatLng
                            ?.let { "%.5f, %.5f".format(it.latitude, it.longitude) }
                            ?: if (isLocating) "Buscando señal GPS del dispositivo…"
                               else "Sin ubicación — toca el mapa o el botón GPS",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            pendingLatLng != null -> MaterialTheme.colorScheme.primary
                            isLocating            -> MaterialTheme.colorScheme.tertiary
                            else                  -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // ── Botones de acción ─────────────────────────────────────────
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick  = { pendingLatLng?.let { onLocationSelected(it) } },
                        enabled  = pendingLatLng != null,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}
