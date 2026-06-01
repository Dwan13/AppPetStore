package com.project.apppetstore.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ── Color schemes ─────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary             = GreenPrimary,
    onPrimary           = GreenOnPrimary,
    primaryContainer    = GreenPrimaryContainer,
    onPrimaryContainer  = GreenOnPrimaryContainer,

    secondary           = GreenSecondary,
    onSecondary         = GreenOnSecondary,
    secondaryContainer  = Color(0xFFE2E8F0),
    onSecondaryContainer= Color(0xFF1E293B),

    tertiary            = GreenTertiary,
    onTertiary          = Color.White,

    background          = AppBackground,
    onBackground        = TextPrimary,

    surface             = AppSurface,
    onSurface           = TextPrimary,
    surfaceVariant      = Color(0xFFF1F5F1),
    onSurfaceVariant    = TextSecondary,

    outline             = Color(0xFFD1D5DB),
    outlineVariant      = Color(0xFFE5E7EB),

    error               = ErrorColor,
    onError             = Color.White,
    errorContainer      = Color(0xFFFFEDED),
    onErrorContainer    = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary             = Color(0xFF8BC34A),
    onPrimary           = Color(0xFF102006),
    primaryContainer    = Color(0xFF35571F),
    onPrimaryContainer  = Color(0xFFDDF0D0),

    secondary           = Color(0xFF9CA3AF),
    onSecondary         = Color.Black,
    secondaryContainer  = Color(0xFF1E293B),
    onSecondaryContainer= Color(0xFFCBD5E1),

    tertiary            = Color(0xFFFFB74D),
    onTertiary          = Color.Black,

    background          = Color(0xFF11140F),
    onBackground        = Color(0xFFE6E6E6),

    surface             = Color(0xFF1A1D17),
    onSurface           = Color(0xFFE6E6E6),
    surfaceVariant      = Color(0xFF252820),
    onSurfaceVariant    = Color(0xFF9CA3AF),

    outline             = Color(0xFF374151),
    outlineVariant      = Color(0xFF2D3748),

    error               = Color(0xFFFF6B6B),
    onError             = Color.Black
)

// ── Shape system (MD3) ────────────────────────────────────────────────────────
//  ExtraSmall  4 dp  → chips, badges
//  Small       8 dp  → small cards, text fields
//  Medium     12 dp  → cards, dialogs
//  Large      16 dp  → sheets, large cards
//  ExtraLarge 28 dp  → FAB, large surfaces

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ── Theme ─────────────────────────────────────────────────────────────────────

@Composable
fun AppPetStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}
