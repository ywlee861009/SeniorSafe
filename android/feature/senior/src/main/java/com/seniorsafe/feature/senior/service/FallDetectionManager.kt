package com.seniorsafe.feature.senior.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class FallDetectionManager(
    private val sensorManager: SensorManager,
    private val onFallDetected: () -> Unit
) : SensorEventListener {

    companion object {
        private const val FREE_FALL_THRESHOLD = 3.0f
        private const val IMPACT_THRESHOLD    = 20.0f
        private const val STILL_GRAVITY_TOLERANCE = 2.5f
        private const val STILL_GYRO_THRESHOLD = 0.7f
        private const val FALL_GYRO_THRESHOLD = 2.5f
        private const val IMPACT_WINDOW_MS    = 2000L
        private const val POST_IMPACT_SETTLE_WINDOW_MS = 1200L
        private const val STILL_DURATION_MS   = 1500L
        private const val GYRO_FRESH_WINDOW_MS = 500L
    }

    private enum class State { NORMAL, FREE_FALL, IMPACT, CHECKING_STILL }

    private var state = State.NORMAL
    private var freeFallTime = 0L
    private var impactTime = 0L
    private var stillStartTime = 0L
    private var gyroscopeAvailable = false
    private var latestGyroMagnitude = 0.0f
    private var peakGyroMagnitude = 0.0f
    private var lastGyroTime = 0L

    fun start() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) return

        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        gyroscopeAvailable = gyroscope?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        } ?: false
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        state = State.NORMAL
        resetMotionWindow()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = System.currentTimeMillis()
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            updateGyroscope(event, now)
            return
        }
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val magnitude = sqrt(
            event.values[0] * event.values[0] +
            event.values[1] * event.values[1] +
            event.values[2] * event.values[2]
        )

        when (state) {
            State.NORMAL -> if (magnitude < FREE_FALL_THRESHOLD) {
                resetMotionWindow()
                state = State.FREE_FALL
                freeFallTime = now
            }
            State.FREE_FALL -> when {
                magnitude > IMPACT_THRESHOLD -> {
                    state = State.IMPACT
                    impactTime = now
                }
                now - freeFallTime > IMPACT_WINDOW_MS -> {
                    state = State.NORMAL
                    resetMotionWindow()
                }
            }
            State.IMPACT -> when {
                isStill(magnitude, now) && hasRotationEvidence() -> {
                    state = State.CHECKING_STILL
                    stillStartTime = now
                }
                now - impactTime > POST_IMPACT_SETTLE_WINDOW_MS -> {
                    state = State.NORMAL
                    resetMotionWindow()
                }
            }
            State.CHECKING_STILL -> when {
                !isStill(magnitude, now) -> {
                    state = State.NORMAL
                    resetMotionWindow()
                }
                now - stillStartTime > STILL_DURATION_MS -> {
                    state = State.NORMAL
                    resetMotionWindow()
                    onFallDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateGyroscope(event: SensorEvent, now: Long) {
        latestGyroMagnitude = sqrt(
            event.values[0] * event.values[0] +
                event.values[1] * event.values[1] +
                event.values[2] * event.values[2]
        )
        lastGyroTime = now
        if (state != State.NORMAL && latestGyroMagnitude > peakGyroMagnitude) {
            peakGyroMagnitude = latestGyroMagnitude
        }
    }

    private fun isStill(accelMagnitude: Float, now: Long): Boolean {
        val gravityStable = abs(accelMagnitude - SensorManager.GRAVITY_EARTH) <= STILL_GRAVITY_TOLERANCE
        val gyroStable = !hasFreshGyroscopeSample(now) || latestGyroMagnitude <= STILL_GYRO_THRESHOLD
        return gravityStable && gyroStable
    }

    private fun hasRotationEvidence(): Boolean {
        return !gyroscopeAvailable || peakGyroMagnitude >= FALL_GYRO_THRESHOLD
    }

    private fun hasFreshGyroscopeSample(now: Long): Boolean {
        return gyroscopeAvailable && now - lastGyroTime <= GYRO_FRESH_WINDOW_MS
    }

    private fun resetMotionWindow() {
        peakGyroMagnitude = 0.0f
    }
}
