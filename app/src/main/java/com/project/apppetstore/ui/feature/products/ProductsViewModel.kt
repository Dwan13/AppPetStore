package com.project.apppetstore.ui.feature.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.project.apppetstore.data.model.Product
import com.project.apppetstore.data.repository.MockPetShopRepository
import com.project.apppetstore.data.repository.PetShopRepository

data class ProductsUiState(
    val filters: List<String> = emptyList(),
    val selectedFilter: String = "Todos",
    val products: List<Product> = emptyList()
)

class ProductsViewModel(
    repository: PetShopRepository = MockPetShopRepository
) : ViewModel() {

    private val allProducts = repository.getProducts()

    var uiState by mutableStateOf(
        ProductsUiState(
            filters = listOf("Todos") + allProducts.map { it.category }.distinct(),
            products = allProducts
        )
    )
        private set

    fun onFilterSelected(filter: String) {
        uiState = uiState.copy(
            selectedFilter = filter,
            products = if (filter == "Todos") {
                allProducts
            } else {
                allProducts.filter { it.category == filter }
            }
        )
    }
}

