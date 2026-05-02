package com.project.apppetstore.ui.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.apppetstore.ui.components.PrimaryButton
import com.project.apppetstore.ui.components.SecondaryButton

@Composable
fun RegisterScreen(
    onRegister: (String, String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )

        PrimaryButton(
            text = "Crear cuenta",
            onClick = {
                if (fullName.isNotBlank() && email.isNotBlank()) {
                    onRegister(fullName.trim(), email.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        SecondaryButton(
            text = "Ya tengo cuenta",
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

