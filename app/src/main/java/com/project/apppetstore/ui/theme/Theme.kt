package com.project.apppetstore.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BC34A),
    onPrimary = Color(0xFF102006),

    primaryContainer = Color(0xFF35571F),
    onPrimaryContainer = Color(0xFFDDF0D0),

    secondary = Color(0xFF9CA3AF),
    onSecondary = Color.Black,

    tertiary = Color(0xFFFFB74D),

    background = Color(0xFF11140F),
    onBackground = Color(0xFFE6E6E6),

    surface = Color(0xFF1A1D17),
    onSurface = Color(0xFFE6E6E6),

    error = Color(0xFFFF6B6B),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,

    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,

    tertiary = GreenTertiary,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = AppSurface,
    onSurface = TextPrimary,

    error = ErrorColor,
    onError = Color.White
)

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
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}