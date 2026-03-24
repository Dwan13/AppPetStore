package com.project.apppetstore.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.project.apppetstore.data.model.Pet

@Composable
fun PetCard(
    pet: Pet,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    image: Painter? = null
) {
    Card(modifier = if (onClick != null) modifier.clickable { onClick() } else modifier) {
        if (image != null) {
            Image(
                painter = image,
                contentDescription = pet.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
            )
        }
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = pet.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${pet.breed} • ${pet.age}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
