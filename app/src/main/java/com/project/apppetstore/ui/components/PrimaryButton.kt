package com.project.apppetstore.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón primario siguiendo la especificación MD3:
 * – Shape full-rounded (50 dp) → "Filled Button"
 * – Altura estándar 48 dp para área táctil cómoda (>= 48 dp recomendado por Android)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(50),   // MD3 Filled Button shape
        enabled = enabled
    ) {
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge
        )
    }
}
