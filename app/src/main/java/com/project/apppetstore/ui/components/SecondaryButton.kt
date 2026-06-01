package com.project.apppetstore.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón secundario siguiendo la especificación MD3:
 * – OutlinedButton con borde del color outline del tema
 * – Shape full-rounded (50 dp) → "Outlined Button"
 * – Altura estándar 48 dp
 */
@Composable
fun SecondaryButton(
    text: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(50),   // MD3 Outlined Button shape
        enabled = enabled
    ) {
        if (content != null) {
            content()
        } else if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
