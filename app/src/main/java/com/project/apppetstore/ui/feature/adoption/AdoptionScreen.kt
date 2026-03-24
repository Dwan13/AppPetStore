package com.project.adopetshop.ui.feature.adoption

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.project.adopetshop.data.model.ChatMessage
import com.project.adopetshop.ui.components.ChatSection
import com.project.adopetshop.ui.components.PetCard
import com.project.adopetshop.ui.components.PrimaryButton

@Composable
fun AdoptionScreen(
    uiState: AdoptionUiState,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier,
    selectedPetId: String? = null,
    onPetSelected: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val petsToShow = if (selectedPetId != null) uiState.pets.filter { it.id == selectedPetId } else uiState.pets
    var isFavorite by remember(selectedPetId) { mutableStateOf(false) }
    val selectedPet = uiState.pets.find { it.id == selectedPetId }
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedPet != null) {
            // Pantalla de detalle con datos
            PetDetailScreen(
                pet = selectedPet,
                isFavorite = isFavorite,
                onToggleFavorite = { isFavorite = !isFavorite },
                onAdoptClick = { },
                onBack = onBack,

                messages = uiState.messages,
                currentInput = uiState.currentInput,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage
            )

        } else {
            Text(
                "Chat de Adopción",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(petsToShow, key = { it.id }) { pet ->
                    PetCard(
                        pet = pet,
                        modifier = Modifier
                            .fillParentMaxWidth(0.65f)
                            .clickable { onPetSelected(pet.id) },
                        image = pet.imageRes?.let { painterResource(it) }
                    )
                }
            }

            ChatSection(
                messages = uiState.messages,
                currentInput = uiState.currentInput,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage
            )
        }
    }
}
