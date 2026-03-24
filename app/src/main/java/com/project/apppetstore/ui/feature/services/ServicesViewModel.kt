package com.project.apppetstore.ui.feature.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.data.repository.MockPetShopRepository
import com.project.apppetstore.data.repository.PetShopRepository

data class ServicesUiState(
    val filters: List<String> = emptyList(),
    val selectedFilter: String = "Todos",
    val services: List<Service> = emptyList()
)

class ServicesViewModel(
    private val repository: PetShopRepository = MockPetShopRepository
) : ViewModel() {

    private val allServices = repository.getServices()

    var uiState by mutableStateOf(
        ServicesUiState(
            filters = listOf("Todos") + allServices.map { it.category }.distinct(),
            services = allServices
        )
    )
        private set

    fun onFilterSelected(filter: String) {
        uiState = uiState.copy(
            selectedFilter = filter,
            services = if (filter == "Todos") {
                allServices
            } else {
                allServices.filter { it.category == filter }
            }
        )
    }
}

