package com.project.apppetstore.ui.feature.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.apppetstore.ui.components.ServiceCard

@Composable
fun ServicesScreen(
    uiState: ServicesUiState,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Servicios", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filters) { filter ->
                    FilterChip(
                        selected = filter == uiState.selectedFilter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter) }
                    )
                }
            }
        }

        items(uiState.services, key = { it.id }) { service ->
            ServiceCard(
                service = service,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

