package com.seniorsafe.feature.senior.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class FallDetectionManager(
    private val sensorManager: SensorManager,
    private val onFallDetected: () -> Unit
) : SensorEventListener {

    companion object {
        private const val FREE_FALL_THRESHOLD = 3.0f
        private const val IMPACT_THRESHOLD    = 20.0f
        private const val STILL_THRESHOLD     = 3.0f
        private const val IMPACT_WINDOW_MS    = 2000L
        private const val STILL_DURATION_MS   = 1500L
    }

    private enum class State { NORMAL, FREE_FALL, IMPACT, CHECKING_STILL }

    private var state = State.NORMAL
    private var freeFallTime = 0L
    private var stillStartTime = 0L

    fun start() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        state = State.NORMAL
    }

    override fun onSensorChanged(event: SensorEvent) {
        val magnitude = sqrt(
            event.values[0] * event.values[0] +
            event.values[1] * event.values[1] +
            event.values[2] * event.values[2]
        )
        val now = System.currentTimeMillis()

        when (state) {
            State.NORMAL -> if (magnitude < FREE_FALL_THRESHOLD) { state = State.FREE_FALL; freeFallTime = now }
            State.FREE_FALL -> when {
                magnitude > IMPACT_THRESHOLD          -> state = State.IMPACT
                now - freeFallTime > IMPACT_WINDOW_MS -> state = State.NORMAL
            }
            State.IMPACT -> if (magnitude < STILL_THRESHOLD) { state = State.CHECKING_STILL; stillStartTime = now } else state = State.NORMAL
            State.CHECKING_STILL -> when {
                magnitude > STILL_THRESHOLD             -> state = State.NORMAL
                now - stillStartTime > STILL_DURATION_MS -> { state = State.NORMAL; onFallDetected() }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
