package com.project.apppetstore.ui.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.data.repository.MockPetShopRepository
import com.project.apppetstore.data.repository.PetShopRepository

data class HomeUiState(
    val filters: List<String> = emptyList(),
    val selectedFilter: String = "Todos",
    val services: List<Service> = emptyList(),
    val pets: List<Pet> = emptyList()
)

class HomeViewModel(
    repository: PetShopRepository = MockPetShopRepository
) : ViewModel() {
    private val allServices = repository.getServices()

    var uiState by androidx.compose.runtime.mutableStateOf(
        HomeUiState(
            filters = listOf("Todos") + allServices.map { it.category }.distinct(),
            services = allServices,
            pets = repository.getPets()
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
