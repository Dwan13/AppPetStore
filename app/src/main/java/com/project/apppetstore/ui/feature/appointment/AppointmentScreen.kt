package com.project.apppetstore.ui.feature.appointment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.apppetstore.data.repository.MockPetShopRepository

// ─────────────────────────────────────────────────────────────────────────────
// Modelos locales
// ─────────────────────────────────────────────────────────────────────────────

private data class DaySlot(val label: String, val dayNum: Int, val monthShort: String)

private val TIME_SLOTS = listOf(
    "08:00", "09:30", "11:00",
    "14:00", "15:30", "17:00"
)

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla principal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AppointmentScreen(
    serviceId      : String?,
    serviceCategory: String?,
    onBack         : () -> Unit = {}
) {
    val service = remember(serviceId) {
        MockPetShopRepository.getServices().find { it.id == serviceId }
    }

    // Próximos 7 días
    val daySlots: List<DaySlot> = remember {
        val dayNames   = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
        val monthNames = listOf("ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic")
        (0..6).map { offset ->
            val cal = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, offset)
            }
            val label = when (offset) {
                0 -> "Hoy"
                1 -> "Mañana"
                else -> dayNames[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            }
            DaySlot(
                label      = label,
                dayNum     = cal.get(java.util.Calendar.DAY_OF_MONTH),
                monthShort = monthNames[cal.get(java.util.Calendar.MONTH)]
            )
        }
    }

    var selectedDayIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedTime     by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmed        by rememberSaveable { mutableStateOf(false) }

    // ── Estado: cita confirmada ────────────────────────────────────────────────
    if (confirmed) {
        ConfirmationScreen(
            serviceName = service?.name ?: (serviceCategory ?: "Servicio"),
            day         = daySlots[selectedDayIndex],
            time        = selectedTime ?: "",
            onBack      = onBack
        )
        return
    }

    // ── Formulario de agendar ──────────────────────────────────────────────────
    Column(modifier = Modifier.fillMaxSize()) {

        // Top bar
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
                text       = "Agendar turno",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Info del servicio ─────────────────────────────────────────────
            Card(
                shape  = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text       = service?.name ?: (serviceCategory ?: "Servicio"),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text  = service?.description ?: serviceCategory ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    if (service != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "⭐ ${service.rating} · ${service.category}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // ── Selección de fecha ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text       = "Selecciona una fecha",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(daySlots) { index, day ->
                        DayChip(
                            day      = day,
                            selected = selectedDayIndex == index,
                            onClick  = { selectedDayIndex = index }
                        )
                    }
                }
            }

            // ── Selección de horario ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text       = "Selecciona un horario",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Grid 3 columnas
                TIME_SLOTS.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { time ->
                            TimeChip(
                                time     = time,
                                selected = selectedTime == time,
                                onClick  = { selectedTime = time },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Rellena si la fila tiene menos de 3 elementos
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // ── Botón confirmar ───────────────────────────────────────────────
            Button(
                onClick  = { confirmed = true },
                enabled  = selectedTime != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text  = "Confirmar cita",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (selectedTime == null) {
                Text(
                    text  = "Selecciona fecha y horario para continuar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chip de día
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DayChip(day: DaySlot, selected: Boolean, onClick: () -> Unit) {
    val bg      = if (selected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant
    val content = if (selected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = bg,
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text       = day.label,
                style      = MaterialTheme.typography.labelSmall,
                color      = content,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text       = "${day.dayNum}",
                style      = MaterialTheme.typography.titleMedium,
                color      = content,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = day.monthShort,
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.8f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chip de hora
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TimeChip(
    time    : String,
    selected: Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg      = if (selected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surface
    val content = if (selected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurface
    val border  = if (selected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = time,
            style      = MaterialTheme.typography.labelLarge,
            color      = content,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla de confirmación
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConfirmationScreen(
    serviceName: String,
    day        : DaySlot,
    time       : String,
    onBack     : () -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter   = scaleIn() + fadeIn()
        ) {
            Surface(
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text       = "¡Cita confirmada!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text      = serviceName,
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Card(
            shape  = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.DateRange, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text  = "${day.label} ${day.dayNum} ${day.monthShort}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Schedule, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text  = time,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(50)
        ) {
            Text("Volver al inicio")
        }
    }
}
