package com.project.apppetstore.ui.feature.home

import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.model.Service
import com.project.apppetstore.data.repository.FirestorePetsRepository
import com.project.apppetstore.data.repository.FirestoreServicesRepository
import com.project.apppetstore.data.repository.MockPetShopRepository
import com.project.apppetstore.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeUiState(
    val filters           : List<String>  = emptyList(),
    val selectedFilter    : String        = "Todos",
    val services          : List<Service> = emptyList(),
    val pets              : List<Pet>     = emptyList(),
    val isLoading         : Boolean       = true,
    /** true una vez que recibimos coordenadas GPS del usuario */
    val hasLocation       : Boolean       = false,
    /** true mientras los servicios se cargan desde Firestore */
    val isServicesLoading : Boolean       = true
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository.getInstance(app)

    /** Radio actual en km (reactivo vía SettingsRepository). */
    private var nearbyRadiusKm : Double = settingsRepo.searchRadius.value.toDouble()

    /** Última posición GPS conocida (para re-filtrar al cambiar el radio). */
    private var lastLat : Double? = null
    private var lastLng : Double? = null

    private var allServices    : List<Service> = emptyList()
    private var nearbyServices : List<Service> = emptyList()
    private var hasLocation    = false
    private var currentFilter  = "Todos"

    /** Ubicación recibida antes de que los servicios terminen de cargar. */
    private var pendingLat : Double? = null
    private var pendingLng : Double? = null

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        // ── Reaccionar a cambios de radio desde Configuración ─────────────────
        viewModelScope.launch {
            settingsRepo.searchRadius.collect { km ->
                nearbyRadiusKm = km.toDouble()
                val lat = lastLat
                val lng = lastLng
                if (lat != null && lng != null && allServices.isNotEmpty()) {
                    applyLocationFilter(lat, lng)
                }
            }
        }

        // ── Carga inicial de datos ────────────────────────────────────────────
        viewModelScope.launch {
            delay(700)
            val ctx = getApplication<Application>()

            // 1. Mascotas primero (el carrusel necesita itemCount correcto)
            val pets = try {
                FirestorePetsRepository.getPets(ctx)
            } catch (_: Exception) {
                MockPetShopRepository.getPets()
            }
            uiState = uiState.copy(isLoading = false, pets = pets)

            // 2. Servicios (skeleton ya visible)
            allServices = try {
                FirestoreServicesRepository.getServices(ctx).sortedByDescending { it.rating }
            } catch (_: Exception) {
                MockPetShopRepository.getServices().sortedByDescending { it.rating }
            }
            uiState = uiState.copy(
                filters           = listOf("Todos") + allServices.map { it.category }.distinct(),
                isServicesLoading = false
            )

            // 3. Aplicar ubicación si ya llegó
            val pLat = pendingLat
            val pLng = pendingLng
            if (pLat != null && pLng != null) applyLocationFilter(pLat, pLng)
            else                               applyFilter()
        }
    }

    fun updateUserLocation(userLat: Double, userLng: Double) {
        if (allServices.isEmpty()) {
            pendingLat = userLat
            pendingLng = userLng
            return
        }
        applyLocationFilter(userLat, userLng)
    }

    fun onFilterSelected(filter: String) {
        currentFilter = filter
        applyFilter()
    }

    // ── Privado ───────────────────────────────────────────────────────────────

    private fun applyLocationFilter(userLat: Double, userLng: Double) {
        lastLat = userLat
        lastLng = userLng
        val buf = FloatArray(1)
        val withDistance = allServices.map { service ->
            if (service.lat != 0.0 && service.lng != 0.0) {
                Location.distanceBetween(userLat, userLng, service.lat, service.lng, buf)
                service.copy(distanceKm = buf[0] / 1_000.0)
            } else service
        }
        nearbyServices = withDistance
            .filter { it.lat != 0.0 && it.distanceKm <= nearbyRadiusKm }
            .sortedBy { it.distanceKm }
        hasLocation = true
        pendingLat  = null
        pendingLng  = null
        applyFilter()
    }

    private fun applyFilter() {
        val base     = if (hasLocation) nearbyServices else allServices
        val filtered = if (currentFilter == "Todos") base
                       else base.filter { it.category == currentFilter }
        uiState = uiState.copy(
            selectedFilter = currentFilter,
            services       = filtered,
            hasLocation    = hasLocation
        )
    }
}
