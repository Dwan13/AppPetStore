package com.project.apppetstore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.project.apppetstore.ui.theme.AppPetStoreTheme

@Composable
fun RequestPermissionCard(modifier: Modifier = Modifier, onRequestPermission: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onTertiary,
        ),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                tint = MaterialTheme.colorScheme.tertiary,
                contentDescription = null
            )
            Text(
                text = "El uso de Localizacion es necesario, por faavor acepte los permisos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
            OutlinedButton(
                onClick = onRequestPermission,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary,
                ),
            ) {
                Text(text = "Solicitar permisos")
            }
        }
    }
}

@PreviewLightDark
@Preview(showBackground = true)
@Composable
fun RequestPermissionCardPreview() {
    AppPetStoreTheme {
        RequestPermissionCard()
    }
}