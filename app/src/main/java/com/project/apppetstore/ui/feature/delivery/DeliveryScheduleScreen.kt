package com.project.apppetstore.ui.feature.delivery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeliveryScheduleScreen(
    serviceId: String?,
    serviceCategory: String?,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var address by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }

    val isFormValid = address.isNotBlank() && date.isNotBlank() && time.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Agendar domicilio",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Servicio seleccionado: ${serviceId ?: "no definido"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Categoria: ${serviceCategory ?: "no definida"}",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Direccion") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Fecha") },
            placeholder = { Text("Ej: 2026-05-01") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Hora") },
            placeholder = { Text("Ej: 14:30") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = onConfirm,
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar agendamiento")
        }
    }
}
