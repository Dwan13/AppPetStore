package com.project.apppetstore.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Motor del shimmer  (siguiendo MD3: animación lineal continua sobre superficies)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun shimmerBrush(baseColor: Color? = null): Brush {
    val color = baseColor ?: MaterialTheme.colorScheme.surfaceVariant

    val shimmerColors = listOf(
        color.copy(alpha = 1.00f),
        color.copy(alpha = 0.35f),
        color.copy(alpha = 1.00f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 1400f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start  = Offset(translateAnim - 500f, translateAnim - 500f),
        end    = Offset(translateAnim, translateAnim)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Elemento base
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton: ServiceCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ServiceCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        SkeletonBox(modifier = Modifier.size(48.dp), shape = CircleShape)

        // Texto
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(14.dp), shape = RoundedCornerShape(4.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.38f).height(12.dp), shape = RoundedCornerShape(4.dp))
        }

        // Botón
        SkeletonBox(modifier = Modifier.width(80.dp).height(36.dp), shape = RoundedCornerShape(50))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton: PetCard  (208 dp de ancho en el carrusel)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PetCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Imagen
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = 0.dp, bottomEnd = 0.dp
            )
        )
        // Info
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.65f).height(16.dp), shape = RoundedCornerShape(4.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.45f).height(12.dp), shape = RoundedCornerShape(4.dp))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBox(modifier = Modifier.width(52.dp).height(24.dp), shape = RoundedCornerShape(50))
                SkeletonBox(modifier = Modifier.width(52.dp).height(24.dp), shape = RoundedCornerShape(50))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton: ProductCard  (mitad de ancho en grid 2 columnas)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Imagen
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = 0.dp, bottomEnd = 0.dp
            )
        )
        // Info
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp), shape = RoundedCornerShape(4.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp), shape = RoundedCornerShape(4.dp))
            Spacer(Modifier.height(4.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.42f).height(18.dp), shape = RoundedCornerShape(4.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton: burbuja de chat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChatBubbleSkeleton(isUser: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier            = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.62f else 0.52f)
                .height(42.dp),
            shape = RoundedCornerShape(
                topStart    = 16.dp,
                topEnd      = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd   = if (isUser) 4.dp  else 16.dp
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton completo: HomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeContentSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Título
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.48f).height(28.dp), shape = RoundedCornerShape(6.dp))

        // Banner del mapa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SkeletonBox(modifier = Modifier.size(52.dp), shape = CircleShape)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.65f).height(16.dp), shape = RoundedCornerShape(4.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.88f).height(12.dp), shape = RoundedCornerShape(4.dp))
            }
            SkeletonBox(modifier = Modifier.size(20.dp), shape = CircleShape)
        }

        // Filtros
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonBox(modifier = Modifier.width(74.dp).height(32.dp), shape = RoundedCornerShape(50))
            }
        }

        // Título sección mascotas
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.45f).height(20.dp), shape = RoundedCornerShape(4.dp))

        // Pet cards (2 en fila)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PetCardSkeleton(modifier = Modifier.weight(1f))
            PetCardSkeleton(modifier = Modifier.weight(1f))
        }

        // Título sección servicios
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp), shape = RoundedCornerShape(4.dp))

        // Service cards
        repeat(3) { ServiceCardSkeleton() }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton completo: ProductsScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductsGridSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Título
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.35f).height(28.dp), shape = RoundedCornerShape(6.dp))

        // Filtros
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                SkeletonBox(modifier = Modifier.width(70.dp).height(32.dp), shape = RoundedCornerShape(50))
            }
        }

        // Grilla 2 columnas
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductCardSkeleton(modifier = Modifier.weight(1f))
                ProductCardSkeleton(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton: mensajes de chat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChatMessagesSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChatBubbleSkeleton(isUser = false)
        ChatBubbleSkeleton(isUser = true)
        ChatBubbleSkeleton(isUser = false)
        ChatBubbleSkeleton(isUser = true)
    }
}
