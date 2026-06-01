package com.project.apppetstore.ui.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.apppetstore.ui.components.PetCard

@Composable
fun FavoritesScreen(
    uiState          : FavoritesUiState,
    onBack           : () -> Unit,
    onFavoriteToggle : (String) -> Unit,
    onPetClick       : (String) -> Unit = {},
    modifier         : Modifier = Modifier
) {
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
                text       = "Favoritos",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider()

        // ── Contenido ─────────────────────────────────────────────────────────
        if (!uiState.isLoading && uiState.favoritePets.isEmpty()) {
            // Estado vacío
            Box(
                modifier          = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment  = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(56.dp)
                    )
                    Text(
                        text      = "Aún no tienes favoritos",
                        style     = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text      = "Toca el corazón en cualquier mascota\npara guardarla aquí",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Grid de mascotas favoritas
            LazyVerticalGrid(
                columns             = GridCells.Fixed(2),
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.favoritePets, key = { it.id }) { pet ->
                    PetCard(
                        pet             = pet,
                        image           = pet.imageRes?.let { painterResource(it) },
                        isFavorite      = true,
                        onFavoriteClick = { onFavoriteToggle(pet.id) },
                        onClick         = { onPetClick(pet.id) }
                    )
                }
            }
        }
    }
}
