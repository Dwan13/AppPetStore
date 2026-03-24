package com.project.apppetstore.ui.feature.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.project.apppetstore.R
import com.project.apppetstore.ui.components.FilterChipWithIcon
import com.project.apppetstore.ui.components.PetCard
import com.project.apppetstore.ui.components.SectionTitle
import com.project.apppetstore.ui.components.ServiceCard

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onPetClick: ((petId: String) -> Unit)? = null,
    onFilterSelected: (String) -> Unit = {}
) {

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
                    .height(180.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(400f, 400f)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filters) { filter ->
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
        
        item { SectionTitle(title = "Mascotas en adopción") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.pets, key = { it.id }) { pet ->
                    PetCard(
                        pet = pet,
                        modifier = Modifier.fillParentMaxWidth(0.62f),
                        onClick = onPetClick?.let { { it(pet.id) } },
                        image = pet.imageRes?.let { painterResource(it) }
                    )
                }
            }
        }
        
        item { SectionTitle(title = "Servicios a domicilio") }
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
