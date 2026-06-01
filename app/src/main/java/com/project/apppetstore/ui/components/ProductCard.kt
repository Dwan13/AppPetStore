package com.project.apppetstore.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil3.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.apppetstore.data.model.Product

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        // Imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            when {
                !product.imageUrl.isNullOrBlank() -> AsyncImage(
                    model              = product.imageUrl,
                    contentDescription = product.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                image != null -> Image(
                    painter            = image,
                    contentDescription = product.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                else -> Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = product.name.take(1),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Badge de descuento
            if (product.discount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "-${product.discount}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2
            )
            Text(
                text = product.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Rating
            if (product.reviewCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${product.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "(${product.reviewCount})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Precio
            val basePrice = product.price.removePrefix("$").toDoubleOrNull() ?: 0.0
            val finalPrice = if (product.discount > 0)
                basePrice * (1 - product.discount / 100.0) else basePrice

            Text(
                text = "$${"%.2f".format(finalPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
