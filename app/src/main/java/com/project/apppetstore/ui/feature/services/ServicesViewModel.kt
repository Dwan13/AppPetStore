package com.project.apppetstore.ui.feature.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.data.repository.FirestoreServicesRepository
import com.project.apppetstore.data.repository.MockPetShopRepository
import kotlinx.coroutines.launch

data class ServicesUiState(
    val filters: List<String> = emptyList(),
    val selectedFilter: String = "Todos",
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = true
)

class ServicesViewModel(app: Application) : AndroidViewModel(app) {

    private var allServices: List<Service> = emptyList()

    var uiState by mutableStateOf(ServicesUiState())
        private set

    init {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            allServices = try {
                FirestoreServicesRepository.getServices(ctx).sortedByDescending { it.rating }
            } catch (_: Exception) {
                MockPetShopRepository.getServices().sortedByDescending { it.rating }
            }
            uiState = ServicesUiState(
                filters = listOf("Todos") + allServices.map { it.category }.distinct(),
                services = allServices,
                isLoading = false
            )
        }
    }

    fun onFilterSelected(filter: String) {
        val filtered = if (filter == "Todos") allServices
        else allServices.filter { it.category == filter }
        uiState = uiState.copy(
            selectedFilter = filter,
            services = filtered
        )
    }
}
