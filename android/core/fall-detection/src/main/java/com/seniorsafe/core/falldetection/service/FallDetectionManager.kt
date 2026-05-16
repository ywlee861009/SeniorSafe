package com.seniorsafe.core.falldetection.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import kotlin.math.sqrt

class FallDetectionManager(
    private val sensorManager: SensorManager,
    private val diagnosticsLogStore: DiagnosticsLogStore,
    private val onFallDetected: () -> Unit
) : SensorEventListener {

    companion object {
        private const val FREE_FALL_THRESHOLD = 3.0f
        private const val IMPACT_THRESHOLD = 20.0f
        private const val STILL_THRESHOLD = 3.0f
        private const val IMPACT_WINDOW_MS = 2000L
        private const val STILL_DURATION_MS = 1500L
    }

    private enum class State { NORMAL, FREE_FALL, IMPACT, CHECKING_STILL }

    private var state = State.NORMAL
    private var freeFallTime = 0L
    private var stillStartTime = 0L
    private var lastSampleLogTime = 0L

    fun start() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            diagnosticsLogStore.add("FallDetection", "accelerometer unavailable")
            return
        }

        val registered = sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        diagnosticsLogStore.add(
            "FallDetection",
            "sensor listener start requested: registered=$registered, sensor=${accelerometer.name}"
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        state = State.NORMAL
        diagnosticsLogStore.add("FallDetection", "sensor listener stopped; state reset")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val magnitude = sqrt(
            event.values[0] * event.values[0] +
                event.values[1] * event.values[1] +
                event.values[2] * event.values[2]
        )
        val now = System.currentTimeMillis()

        if (now - lastSampleLogTime >= 1000L) {
            lastSampleLogTime = now
            diagnosticsLogStore.add(
                "SensorSample",
                "state=$state, magnitude=${"%.2f".format(magnitude)}, x=${"%.2f".format(event.values[0])}, y=${"%.2f".format(event.values[1])}, z=${"%.2f".format(event.values[2])}"
            )
        }

        when (state) {
            State.NORMAL -> if (magnitude < FREE_FALL_THRESHOLD) {
                transitionTo(State.FREE_FALL, "free fall threshold crossed: magnitude=${"%.2f".format(magnitude)}")
                freeFallTime = now
            }
            State.FREE_FALL -> when {
                magnitude > IMPACT_THRESHOLD -> transitionTo(State.IMPACT, "impact threshold crossed: magnitude=${"%.2f".format(magnitude)}")
                now - freeFallTime > IMPACT_WINDOW_MS -> transitionTo(State.NORMAL, "impact window expired")
            }
            State.IMPACT -> if (magnitude < STILL_THRESHOLD) {
                transitionTo(State.CHECKING_STILL, "stillness check started: magnitude=${"%.2f".format(magnitude)}")
                stillStartTime = now
            } else {
                transitionTo(State.NORMAL, "post-impact movement resumed: magnitude=${"%.2f".format(magnitude)}")
            }
            State.CHECKING_STILL -> when {
                magnitude > STILL_THRESHOLD -> transitionTo(State.NORMAL, "stillness interrupted: magnitude=${"%.2f".format(magnitude)}")
                now - stillStartTime > STILL_DURATION_MS -> {
                    transitionTo(State.NORMAL, "fall confirmed after stillness duration")
                    onFallDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        diagnosticsLogStore.add(
            "FallDetection",
            "sensor accuracy changed: sensor=${sensor?.name ?: "unknown"}, accuracy=$accuracy"
        )
    }

    private fun transitionTo(nextState: State, reason: String) {
        val previousState = state
        state = nextState
        diagnosticsLogStore.add("FallDetection", "$previousState -> $nextState; $reason")
    }
}
