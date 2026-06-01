package com.project.apppetstore.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState               : SettingsUiState,
    onBack                : () -> Unit,
    onSetSearchRadius     : (Int) -> Unit,
    onSetDarkTheme        : (Boolean?) -> Unit,
    onSetNotifications    : (Boolean) -> Unit,
    modifier              : Modifier = Modifier
) {
    val radiusOptions = listOf(2, 5, 10, 20)
    val themeOptions  = listOf<Boolean?>(null, false, true)
    val themeLabels   = listOf("Sistema", "Claro", "Oscuro")

    Column(modifier = modifier.fillMaxSize()) {

        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text       = "Configuración",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Apariencia ────────────────────────────────────────────────────
            SettingSection(title = "Apariencia") {
                Text("Tema de la aplicación", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    themeOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = uiState.darkTheme == option,
                            onClick  = { onSetDarkTheme(option) },
                            shape    = SegmentedButtonDefaults.itemShape(index, themeOptions.size),
                            label    = { Text(themeLabels[index]) }
                        )
                    }
                }
            }

            // ── Servicios cercanos ────────────────────────────────────────────
            SettingSection(title = "Servicios cercanos") {
                Text(
                    "Radio de búsqueda",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Muestra servicios dentro de este radio desde tu ubicación",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    radiusOptions.forEachIndexed { index, radius ->
                        SegmentedButton(
                            selected = uiState.searchRadiusKm == radius,
                            onClick  = { onSetSearchRadius(radius) },
                            shape    = SegmentedButtonDefaults.itemShape(index, radiusOptions.size),
                            label    = { Text("$radius km") }
                        )
                    }
                }
            }

            // ── Notificaciones ────────────────────────────────────────────────
            SettingSection(title = "Notificaciones") {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("Alertas de citas y pedidos", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Recibe avisos sobre el estado de tus servicios y compras",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked         = uiState.notificationsEnabled,
                        onCheckedChange = onSetNotifications
                    )
                }
            }
        }
    }
}

// ── Sección con tarjeta ───────────────────────────────────────────────────────

@Composable
private fun SettingSection(
    title   : String,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary
        )
        ElevatedCard(
            modifier  = Modifier.fillMaxWidth(),
            shape     = MaterialTheme.shapes.large,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                content()
            }
        }
    }
}
