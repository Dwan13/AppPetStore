package com.project.apppetstore.ui.feature.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.ui.components.ServiceCard
import com.project.apppetstore.ui.components.ServiceCardSkeleton

@Composable
fun ServicesScreen(
    uiState          : ServicesUiState,
    onFilterSelected : (String) -> Unit,
    onScheduleService: (Service) -> Unit = {},
    modifier         : Modifier = Modifier
) {
    LazyColumn(
        modifier            = modifier,
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Servicios", style = MaterialTheme.typography.headlineSmall)
        }

        // Chips de filtro — solo cuando los datos ya cargaron
        if (!uiState.isLoading) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.filters) { filter ->
                        FilterChip(
                            selected = filter == uiState.selectedFilter,
                            onClick  = { onFilterSelected(filter) },
                            label    = { Text(filter) }
                        )
                    }
                }
            }
        }

        // Contenido principal
        if (uiState.isLoading) {
            items(6) {
                ServiceCardSkeleton(modifier = Modifier.padding(top = 4.dp))
            }
        } else {
            items(uiState.services, key = { it.id }) { service ->
                ServiceCard(
                    service         = service,
                    onScheduleClick = { onScheduleService(service) },
                    modifier        = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
