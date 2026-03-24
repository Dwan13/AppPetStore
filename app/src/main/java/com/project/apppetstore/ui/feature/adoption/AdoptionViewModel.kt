package com.project.apppetstore.ui.feature.adoption

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.apppetstore.data.model.ChatMessage
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.repository.MockPetShopRepository
import com.project.apppetstore.data.repository.PetShopRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AdoptionUiState(
    val pets: List<Pet> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentInput: String = ""
)

class AdoptionViewModel(
    repository: PetShopRepository = MockPetShopRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        AdoptionUiState(
            pets = repository.getPets(),
            messages = repository.getInitialChat()
        )
    )
        private set

    fun onInputChange(input: String) {
        uiState = uiState.copy(currentInput = input)
    }

    fun sendMessage() {
        val message = uiState.currentInput.trim()
        if (message.isEmpty()) return

        val newMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            message = message,
            isUser = true
        )
        uiState = uiState.copy(
            currentInput = "",
            messages = uiState.messages + newMessage
        )
        // Simular respuesta automática
        viewModelScope.launch {
            delay(900)
            val autoReply = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                message = "¡Gracias por tu mensaje! Pronto te contactaremos para la adopción.",
                isUser = false
            )
            uiState = uiState.copy(
                messages = uiState.messages + autoReply
            )
        }
    }
}
