package com.project.apppetstore.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocationState(
    val isRecording: Boolean = false,
    val route: List<Location> = emptyList()
)

class LocationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LocationState())
    val state = _uiState.asStateFlow()

    private val maxRoutePoints = 1000

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    fun setup(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply{
            setMinUpdateDistanceMeters(5F)
            setMinUpdateIntervalMillis(1000)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                _uiState.update { currentState ->
                    Log.d("LocationViewModel", "onLocationResult: $locationResult")
                    currentState.copy(
                        route = (currentState.route + locationResult.locations)
                            .takeLast(maxRoutePoints)
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!::fusedLocationClient.isInitialized || !::locationRequest.isInitialized || !::locationCallback.isInitialized) {
            return
        }
        if (_uiState.value.isRecording) return

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        _uiState.update { it.copy(isRecording = true) }
    }

    fun stopLocationUpdates() {
        if (!::fusedLocationClient.isInitialized || !::locationCallback.isInitialized) {
            return
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _uiState.update { it.copy(isRecording = false) }
    }

    fun clearRoute() {
        _uiState.update {
            it.copy(route = emptyList())
        }
    }
}