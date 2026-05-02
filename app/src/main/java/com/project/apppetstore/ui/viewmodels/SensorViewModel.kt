package com.project.apppetstore.ui.viewmodels

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sqrt

data class SensorUiState(
    val isAccelerometerAvailable: Boolean = false,
    val isGyroscopeAvailable: Boolean = false,
    val isLightSensorAvailable: Boolean = false,
    val accelerometerMagnitude: Float? = null,
    val gyroscopeMagnitude: Float? = null,
    val lightLux: Float? = null,
    val isDarkEnvironment: Boolean = false
)

class SensorViewModel : ViewModel(), SensorEventListener {
    private val _uiState = MutableStateFlow(SensorUiState())
    val state = _uiState.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var lightSensor: Sensor? = null
    private var isListening = false

    private var lastAccelUpdateMs = 0L
    private var lastGyroUpdateMs = 0L
    private var lastLightUpdateMs = 0L

    fun setup(context: Context) {
        if (sensorManager != null) return

        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        sensorManager = manager
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        lightSensor = manager.getDefaultSensor(Sensor.TYPE_LIGHT)

        _uiState.update {
            it.copy(
                isAccelerometerAvailable = accelerometer != null,
                isGyroscopeAvailable = gyroscope != null,
                isLightSensorAvailable = lightSensor != null
            )
        }
    }

    fun startListening() {
        if (isListening) return
        val manager = sensorManager ?: return

        accelerometer?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        isListening = true
    }

    fun stopListening() {
        val manager = sensorManager ?: return
        manager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentEvent = event ?: return
        val now = SystemClock.elapsedRealtime()

        when (currentEvent.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if (now - lastAccelUpdateMs < 250) return
                lastAccelUpdateMs = now

                val x = currentEvent.values.getOrNull(0) ?: return
                val y = currentEvent.values.getOrNull(1) ?: return
                val z = currentEvent.values.getOrNull(2) ?: return
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                _uiState.update { it.copy(accelerometerMagnitude = magnitude) }
            }

            Sensor.TYPE_GYROSCOPE -> {
                if (now - lastGyroUpdateMs < 250) return
                lastGyroUpdateMs = now

                val x = currentEvent.values.getOrNull(0) ?: return
                val y = currentEvent.values.getOrNull(1) ?: return
                val z = currentEvent.values.getOrNull(2) ?: return
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                _uiState.update { it.copy(gyroscopeMagnitude = magnitude) }
            }

            Sensor.TYPE_LIGHT -> {
                if (now - lastLightUpdateMs < 400) return
                lastLightUpdateMs = now

                val sampleLux = currentEvent.values.firstOrNull() ?: return
                _uiState.update { previous ->
                    val filteredLux = if (previous.lightLux == null) {
                        sampleLux
                    } else {
                        (previous.lightLux * 0.8f) + (sampleLux * 0.2f)
                    }

                    val isDark = when {
                        filteredLux < 30f -> true
                        filteredLux > 80f -> false
                        else -> previous.isDarkEnvironment
                    }

                    previous.copy(
                        lightLux = filteredLux,
                        isDarkEnvironment = isDark
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}

