package com.project.visprapp.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CombinedSensRepository(context: Context, private val wbSize: Int) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val emfRepository = EmfRepository(context)

    private val barometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private fun normalize(value: Float, scale: Float): Float {
        val scaledValue = value * scale
        return (1.0f - exp(-scaledValue.toDouble())).toFloat()
    }

    private val environmentalFlow: Flow<Pair<Float, Float>> = callbackFlow {
        var smoothedPressure = -1f
        var smoothedLight = -1f
        val alpha = 0.1f

        var currentPressureDelta = 0f
        var currentLightDelta = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_PRESSURE -> {
                        val currentPressure = event.values[0]
                        if (smoothedPressure == -1f) {
                            smoothedPressure = currentPressure
                        } else {
                            smoothedPressure = smoothedPressure * (1 - alpha) + currentPressure * alpha
                        }
                        currentPressureDelta = abs(currentPressure - smoothedPressure)
                    }
                    Sensor.TYPE_LIGHT -> {
                        val currentLux = event.values[0]
                        if (smoothedLight == -1f) {
                            smoothedLight = currentLux
                        } else {
                            smoothedLight = smoothedLight * (1 - alpha) + currentLux * alpha
                        }
                        currentLightDelta = abs(currentLux - smoothedLight)
                    }
                }
                trySend(Pair(currentPressureDelta, currentLightDelta))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        barometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun getEmfDeltaFlow(): Flow<Float> = callbackFlow {
        var smoothedEmf = -1f
        val alpha = 0.1f

        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            emfRepository.getEmfFlow().collect { currentEmf ->
                if (smoothedEmf == -1f) {
                    smoothedEmf = currentEmf
                } else {
                    smoothedEmf = smoothedEmf * (1 - alpha) + currentEmf * alpha
                }
                val emfDelta = abs(currentEmf - smoothedEmf)
                trySend(emfDelta)
            }
        }

        awaitClose {
            job.cancel()
        }
    }

    fun getCombinedWordIndexFlow(): Flow<Int> {
        var wasAboveThreshold = false
        val triggerThreshold = 0.30f
        val resetThreshold = 0.10f

        return combine(getEmfDeltaFlow(), environmentalFlow) { emfDelta, envData ->
            val (pressureDelta, lightDelta) = envData

            val normEmf = normalize(emfDelta, 0.05f)
            val normPressure = normalize(pressureDelta, 0.01f)
            val normLight = normalize(lightDelta, 0.02f)

            (normEmf + normPressure + normLight) / 3.0f
        }.filter { score ->
            val isCurrentlyAbove = score > triggerThreshold

            val shouldTrigger = if (!wasAboveThreshold && isCurrentlyAbove) {
                wasAboveThreshold = true
                true
            } else {
                if (score < resetThreshold) {
                    wasAboveThreshold = false
                }
                false
            }
            shouldTrigger
        }.map { score ->
            (score * wbSize).toInt().coerceIn(0, wbSize - 1)
        }
    }
}