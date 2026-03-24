package com.project.apppetstore.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.R

@Composable
fun ServiceCard(
    service: Service,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageResId = try {
                if (service.imageRes != 0) service.imageRes else R.drawable.ic_user_round
            } catch (e: Exception) {
                R.drawable.ic_user_round
            }
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = service.name,
                modifier = Modifier
                    .height(64.dp)
                    .width(64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = service.name, style = MaterialTheme.typography.titleMedium)
                Text(text = service.category, style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.star),
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.height(16.dp)
                    )
                    Text(text = "${service.rating}", fontSize = 12.sp, color = Color(0xFF444444), modifier = Modifier.padding(start = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(painter = painterResource(R.drawable.ic_map_pin), contentDescription = "Distancia", modifier = Modifier.height(16.dp))
                    Text(text = "${service.distanceKm}km", fontSize = 12.sp, color = Color(0xFF444444), modifier = Modifier.padding(start = 2.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { }, modifier = Modifier.height(32.dp)) {
                Text("Agendar", fontSize = 12.sp)
            }
        }
    }
}
