package com.example.wearrr.fall

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

class FallDetector(
    private val sensorManager: SensorManager,
    private val onFallDetected: (acceleration: Double, gyro: Double) -> Unit
) : SensorEventListener {

    companion object {
        private const val TAG = "FallDetector"

        private const val HIGH_IMPACT_THRESHOLD = 25.0
        private const val FREE_FALL_THRESHOLD = 4.0
        private const val GYRO_THRESHOLD = 4.0
        private const val GRAVITY_BASELINE = 9.81

        private const val HISTORY_SIZE = 10
        private const val MIN_FREEFALL_DURATION = 300L
        private const val MAX_FREEFALL_DURATION = 2000L
        private const val COOLDOWN_MS = 10_000L
    }

    private var lastAcceleration = FloatArray(3)
    private var lastGyroscope = FloatArray(3)
    private var lastFallDetectionTime = 0L

    private val accelerationHistory = mutableListOf<Double>()
    private val gyroHistory = mutableListOf<Double>()
    private var isInFreeFall = false
    private var freeFallStartTime = 0L

    var simulationMode = false
        private set

    private var logCounter = 0

    fun start() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel != null) {
            val ok = sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Accelerometer registered: $ok")
        } else {
            Log.e(TAG, "No accelerometer available!")
        }

        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyro != null) {
            val ok = sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Gyroscope registered: $ok")
        } else {
            Log.w(TAG, "No gyroscope available — tumbling detection disabled")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setSimulation(enabled: Boolean) {
        simulationMode = enabled
        Log.d(TAG, "Simulation mode: $enabled")
    }

    fun triggerSimulatedFall(fallType: String) {
        Log.d(TAG, "Simulating fall: $fallType")
        resetState()
        lastFallDetectionTime = 0L // bypass cooldown for simulation

        when (fallType) {
            "high_impact" -> {
                lastAcceleration = floatArrayOf(8.0f, -12.0f, 30.0f)
                lastGyroscope = floatArrayOf(2.0f, -3.0f, 4.5f)
                checkForFall()
            }
            "gradual_fall" -> {
                simulateGradualFall()
            }
            "false_positive_test" -> {
                lastAcceleration = floatArrayOf(1.5f, 2.5f, 11.0f)
                lastGyroscope = floatArrayOf(0.3f, 0.8f, 1.2f)
                checkForFall()
            }
        }
    }

    private fun simulateGradualFall() {
        CoroutineScope(Dispatchers.IO).launch {
            lastAcceleration = floatArrayOf(1.0f, 2.0f, 10.0f)
            lastGyroscope = floatArrayOf(0.5f, 1.0f, 0.8f)
            checkForFall()
            delay(100)

            lastAcceleration = floatArrayOf(0.5f, 0.8f, 2.5f)
            lastGyroscope = floatArrayOf(1.5f, 2.0f, 1.8f)
            checkForFall()
            delay(200)

            lastAcceleration = floatArrayOf(0.3f, 0.6f, 3.2f)
            lastGyroscope = floatArrayOf(2.0f, 2.5f, 2.2f)
            checkForFall()
            delay(200)

            lastAcceleration = floatArrayOf(4.0f, -6.0f, 18.0f)
            lastGyroscope = floatArrayOf(3.5f, -4.2f, 5.0f)
            checkForFall()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (simulationMode) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAcceleration = event.values.clone()
                checkForFall()
            }
            Sensor.TYPE_GYROSCOPE -> {
                lastGyroscope = event.values.clone()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun checkForFall() {
        val totalAcceleration = sqrt(
            lastAcceleration[0].pow(2) +
            lastAcceleration[1].pow(2) +
            lastAcceleration[2].pow(2)
        ).toDouble()

        val totalGyro = sqrt(
            lastGyroscope[0].pow(2) +
            lastGyroscope[1].pow(2) +
            lastGyroscope[2].pow(2)
        ).toDouble()

        val currentTime = System.currentTimeMillis()

        addToHistory(totalAcceleration, totalGyro)

        val fallDetected = detectAdvancedFall(totalAcceleration, totalGyro, currentTime)
        val cooldownPassed = (currentTime - lastFallDetectionTime) > COOLDOWN_MS

        if (++logCounter % 30 == 0) {
            val mode = if (simulationMode) "[SIM]" else "[REAL]"
            Log.d(TAG, "$mode accel=%.2f gyro=%.2f freeFall=%b avgAccel=%.2f".format(
                totalAcceleration, totalGyro, isInFreeFall, getAverageAcceleration()
            ))
        }

        if (cooldownPassed && fallDetected) {
            lastFallDetectionTime = currentTime
            resetState()
            onFallDetected(totalAcceleration, totalGyro)
        }
    }

    private fun detectAdvancedFall(accel: Double, gyro: Double, currentTime: Long): Boolean {
        // Method 1: Extremely high impact
        if (accel > HIGH_IMPACT_THRESHOLD) {
            Log.d(TAG, "HIGH IMPACT detected: $accel")
            return true
        }

        // Method 2: Free-fall followed by impact
        val isCurrentlyFreeFall = accel < FREE_FALL_THRESHOLD

        if (isCurrentlyFreeFall && !isInFreeFall) {
            isInFreeFall = true
            freeFallStartTime = currentTime
            Log.d(TAG, "Free-fall started")
        } else if (!isCurrentlyFreeFall && isInFreeFall) {
            val freeFallDuration = currentTime - freeFallStartTime
            isInFreeFall = false
            Log.d(TAG, "Free-fall ended, duration: ${freeFallDuration}ms")

            if (freeFallDuration in MIN_FREEFALL_DURATION..MAX_FREEFALL_DURATION &&
                accel > GRAVITY_BASELINE * 1.5 &&
                gyro > GYRO_THRESHOLD
            ) {
                Log.d(TAG, "Valid fall pattern: freefall ${freeFallDuration}ms + impact")
                return true
            }
        }

        // Method 3: Sustained tumbling (high variance + high rotation)
        if (accelerationHistory.size >= HISTORY_SIZE) {
            val variance = calculateAccelerationVariance()
            val avgGyro = getAverageGyro()

            if (variance > 50.0 && avgGyro > GYRO_THRESHOLD * 1.5) {
                Log.d(TAG, "Tumbling pattern detected: variance=%.2f avgGyro=%.2f".format(variance, avgGyro))
                return true
            }
        }

        return false
    }

    private fun addToHistory(accel: Double, gyro: Double) {
        accelerationHistory.add(accel)
        gyroHistory.add(gyro)
        if (accelerationHistory.size > HISTORY_SIZE) accelerationHistory.removeAt(0)
        if (gyroHistory.size > HISTORY_SIZE) gyroHistory.removeAt(0)
    }

    private fun getAverageAcceleration(): Double {
        return if (accelerationHistory.isNotEmpty()) accelerationHistory.average() else GRAVITY_BASELINE
    }

    private fun getAverageGyro(): Double {
        return if (gyroHistory.isNotEmpty()) gyroHistory.average() else 0.0
    }

    private fun calculateAccelerationVariance(): Double {
        if (accelerationHistory.size < 2) return 0.0
        val mean = accelerationHistory.average()
        return accelerationHistory.map { (it - mean).pow(2) }.average()
    }

    private fun resetState() {
        isInFreeFall = false
        freeFallStartTime = 0L
        accelerationHistory.clear()
        gyroHistory.clear()
    }
}
