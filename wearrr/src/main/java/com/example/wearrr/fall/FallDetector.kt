package com.example.wearrr.fall

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Exponential Moving Average filter to smooth sensor noise
 * while preserving sharp fall signatures.
 */
private class EmaFilter(private val alpha: Float = 0.2f) {
    private var filtered: FloatArray? = null

    fun apply(raw: FloatArray): FloatArray {
        val prev = filtered
        val result = if (prev == null) {
            raw.clone()
        } else {
            FloatArray(raw.size) { i -> alpha * raw[i] + (1 - alpha) * prev[i] }
        }
        filtered = result
        return result
    }
}

class FallDetector(
    private val sensorManager: SensorManager,
    private val onFallDetected: () -> Unit
) : SensorEventListener {

    // --- Free-fall + impact state ---
    private var freeFallStartTime = 0L
    private var freeFallEndTime = 0L
    private var inFreeFall = false
    private var waitingForImpact = false

    // --- Immobility verification state ---
    private var fallSuspected = false
    private var suspectTime = 0L
    private val recentMagnitudes = ArrayDeque<Float>(IMMOBILITY_SAMPLES)

    // --- Orientation state (from gravity sensor) ---
    private var isVertical = true
    private var lastOrientationChangeTime = 0L

    // --- General ---
    private var lastDetectionTime = 0L
    private var logCounter = 0
    private val accelFilter = EmaFilter(alpha = 0.2f)

    companion object {
        private const val TAG = "FallDetector"

        // TODO: restore to 3.0f / 25.0f / 80L for production
        private const val FREE_FALL_THRESHOLD = 6.0f      // m/s² (lowered for testing)
        private const val IMPACT_THRESHOLD = 15.0f          // m/s² (lowered for testing)
        private const val FREE_FALL_MIN_DURATION_MS = 50L   // lowered for testing
        private const val IMPACT_WINDOW_MS = 500L
        private const val COOLDOWN_MS = 30_000L

        // Orientation thresholds (gravity sensor z-axis)
        private const val VERTICAL_THRESHOLD = 7.0f    // z > 7 → upright
        private const val HORIZONTAL_THRESHOLD = 3.0f  // |z| < 3 → lying flat
        private const val ORIENTATION_WINDOW_MS = 2_000L

        // Post-fall immobility check
        private const val IMMOBILITY_CHECK_MS = 3_000L
        private const val IMMOBILITY_SAMPLES = 60         // ~3s at 50Hz sampling every other event
        private const val IMMOBILITY_VARIANCE_THRESHOLD = 2.0f
    }

    fun start() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel == null) {
            Log.e(TAG, "No accelerometer sensor available!")
            return
        }
        val accelOk = sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        Log.d(TAG, "Accelerometer listener registered: $accelOk")

        // Gravity sensor for orientation — optional, not all devices have it
        val gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if (gravity != null) {
            val gravOk = sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME)
            Log.d(TAG, "Gravity sensor listener registered: $gravOk")
        } else {
            Log.w(TAG, "No gravity sensor — orientation detection disabled")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_GRAVITY -> handleGravity(event)
        }
    }

    // ──────────────────────────────────────────────
    // Gravity → orientation tracking
    // ──────────────────────────────────────────────
    private fun handleGravity(event: SensorEvent) {
        val z = event.values[2]
        val now = System.currentTimeMillis()

        if (isVertical && abs(z) < HORIZONTAL_THRESHOLD) {
            // Went from vertical to horizontal
            isVertical = false
            lastOrientationChangeTime = now
            Log.d(TAG, "Orientation: vertical → horizontal (z=%.2f)".format(z))
        } else if (!isVertical && z > VERTICAL_THRESHOLD) {
            isVertical = true
            lastOrientationChangeTime = now
        }
    }

    private fun hadRecentOrientationChange(now: Long): Boolean {
        return !isVertical && (now - lastOrientationChangeTime) < ORIENTATION_WINDOW_MS
    }

    // ──────────────────────────────────────────────
    // Accelerometer → fall detection pipeline
    // ──────────────────────────────────────────────
    private fun handleAccelerometer(event: SensorEvent) {
        val smoothed = accelFilter.apply(event.values)
        val magnitude = sqrt(
            smoothed[0] * smoothed[0] +
            smoothed[1] * smoothed[1] +
            smoothed[2] * smoothed[2]
        )
        val now = System.currentTimeMillis()

        // Periodic logging (~every 2s)
        if (++logCounter % 100 == 0) {
            Log.d(TAG, "mag=%.2f  freeFall=%b  suspect=%b  vert=%b".format(
                magnitude, inFreeFall, fallSuspected, isVertical
            ))
        }

        // Feed immobility buffer
        trackMagnitude(magnitude)

        // If we're in the immobility-verification window, check that instead
        if (fallSuspected) {
            checkImmobility(now)
            return   // don't start new detection while verifying
        }

        // Cooldown
        if (lastDetectionTime != 0L && now - lastDetectionTime < COOLDOWN_MS) return

        // ── Method 1: free-fall → impact ──
        if (magnitude < FREE_FALL_THRESHOLD) {
            if (!inFreeFall) {
                inFreeFall = true
                freeFallStartTime = now
                Log.d(TAG, "Free-fall started (mag=%.2f)".format(magnitude))
            }
        } else {
            if (inFreeFall) {
                val duration = now - freeFallStartTime
                if (duration >= FREE_FALL_MIN_DURATION_MS) {
                    waitingForImpact = true
                    freeFallEndTime = now
                    Log.d(TAG, "Free-fall ended after %dms, watching for impact".format(duration))
                }
                inFreeFall = false
            }
        }

        if (waitingForImpact) {
            if (now - freeFallEndTime > IMPACT_WINDOW_MS) {
                waitingForImpact = false
            } else if (magnitude > IMPACT_THRESHOLD) {
                Log.d(TAG, "Impact after free-fall (mag=%.2f) — starting immobility check".format(magnitude))
                waitingForImpact = false
                beginImmobilityCheck(now)
                return
            }
        }

        // ── Method 2: orientation change + moderate impact ──
        if (hadRecentOrientationChange(now) && magnitude > IMPACT_THRESHOLD * 0.7f) {
            Log.d(TAG, "Orientation change + impact (mag=%.2f) — starting immobility check".format(magnitude))
            beginImmobilityCheck(now)
            return
        }

        // ── Method 3: very high single impact (> 1.5x threshold) ──
        if (magnitude > IMPACT_THRESHOLD * 1.5f) {
            Log.d(TAG, "Very high impact (mag=%.2f) — starting immobility check".format(magnitude))
            beginImmobilityCheck(now)
        }
    }

    // ──────────────────────────────────────────────
    // Immobility verification (reduces false positives)
    // ──────────────────────────────────────────────
    private fun beginImmobilityCheck(now: Long) {
        fallSuspected = true
        suspectTime = now
        recentMagnitudes.clear()
        Log.d(TAG, "Immobility check started (${IMMOBILITY_CHECK_MS}ms window)")
    }

    private fun trackMagnitude(mag: Float) {
        if (recentMagnitudes.size >= IMMOBILITY_SAMPLES) recentMagnitudes.removeFirst()
        recentMagnitudes.addLast(mag)
    }

    private fun checkImmobility(now: Long) {
        val elapsed = now - suspectTime

        if (elapsed < 1_000) return // wait at least 1s before judging

        if (elapsed >= IMMOBILITY_CHECK_MS) {
            // Time's up — check variance
            if (recentMagnitudes.size >= 10) {
                val mean = recentMagnitudes.average().toFloat()
                val variance = recentMagnitudes.map { (it - mean) * (it - mean) }.average().toFloat()

                if (variance < IMMOBILITY_VARIANCE_THRESHOLD) {
                    Log.d(TAG, "Immobility confirmed (variance=%.3f) — FALL DETECTED".format(variance))
                    lastDetectionTime = now
                    fallSuspected = false
                    onFallDetected()
                    return
                } else {
                    Log.d(TAG, "Movement after suspected fall (variance=%.3f) — false positive".format(variance))
                }
            }
            // Not immobile or not enough data → dismiss
            fallSuspected = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
