package com.project.apppetstore.ui.feature.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.apppetstore.data.model.Product
import com.project.apppetstore.data.repository.FirestoreProductsRepository
import com.project.apppetstore.data.repository.MockPetShopRepository
import kotlinx.coroutines.launch

data class ProductsUiState(
    val filters: List<String> = emptyList(),
    val selectedFilter: String = "Todos",
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true
)

class ProductsViewModel(app: Application) : AndroidViewModel(app) {

    private var allProducts: List<Product> = emptyList()

    var uiState by mutableStateOf(ProductsUiState())
        private set

    init {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            allProducts = try {
                FirestoreProductsRepository.getProducts(ctx)
            } catch (_: Exception) {
                MockPetShopRepository.getProducts()
            }
            uiState = ProductsUiState(
                filters = listOf("Todos") + allProducts.map { it.category }.distinct(),
                products = allProducts,
                isLoading = false
            )
        }
    }

    fun getProductById(id: String) = allProducts.find { it.id == id }

    fun onFilterSelected(filter: String) {
        val filtered = if (filter == "Todos") allProducts
        else allProducts.filter { it.category == filter }
        uiState = uiState.copy(
            selectedFilter = filter,
            products = filtered
        )
    }
}
