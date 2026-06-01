package com.project.apppetstore.ui.feature.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.project.apppetstore.ui.components.ProductCard
import com.project.apppetstore.ui.components.ProductsGridSkeleton

@Composable
fun ProductsScreen(
    uiState: ProductsUiState,
    onFilterSelected: (String) -> Unit,
    onProductClick: (productId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Skeleton mientras carga ──────────────────────────────────────
        if (uiState.isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProductsGridSkeleton()
            }
            return@LazyVerticalGrid
        }

        // ── Contenido real ───────────────────────────────────────────────
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text("Productos", style = MaterialTheme.typography.headlineSmall)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filters.size) { index ->
                    val filter = uiState.filters[index]
                    FilterChip(
                        selected = filter == uiState.selectedFilter,
                        onClick  = { onFilterSelected(filter) },
                        label    = { Text(filter) }
                    )
                }
            }
        }

        items(uiState.products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                image   = product.imageRes?.let { painterResource(it) },
                onClick = { onProductClick(product.id) }
            )
        }
    }
}
