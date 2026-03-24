package com.project.adopetshop.ui.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.project.adopetshop.data.model.Pet
import com.project.adopetshop.data.model.Service
import com.project.adopetshop.data.repository.MockPetShopRepository
import com.project.adopetshop.data.repository.PetShopRepository

data class HomeUiState(
    val filters: List<String> = emptyList(),
    val selectedFilter: String = "All",
    val services: List<Service> = emptyList(),
    val pets: List<Pet> = emptyList()
)

class HomeViewModel(
    repository: PetShopRepository = MockPetShopRepository
) : ViewModel() {
    private val allServices = repository.getServices()

    var uiState by androidx.compose.runtime.mutableStateOf(
        HomeUiState(
            filters = listOf("All") + allServices.map { it.category }.distinct(),
            services = allServices,
            pets = repository.getPets()
        )
    )
        private set

    fun onFilterSelected(filter: String) {
        uiState = uiState.copy(
            selectedFilter = filter,
            services = if (filter == "All") {
                allServices
            } else {
                allServices.filter { it.category == filter }
            }
        )
    }
}
