package com.example.wearrr.fall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FallDetectionService : Service() {

    private lateinit var fallDetector: FallDetector
    private lateinit var vibrator: Vibrator
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var alertInProgress = false

    companion object {
        private const val TAG = "FallDetectionService"
        private const val CHANNEL_ID = "fall_detection_channel"
        private const val NOTIFICATION_ID = 1001
        private const val COOLDOWN_MS = 30_000L

        private var instance: FallDetectionService? = null
        private var lastAlertTime = 0L

        fun simulateFall(fallType: String = "high_impact") {
            Log.d(TAG, "Simulating fall: $fallType")
            // Reset service-level cooldown so simulation always works
            lastAlertTime = 0L
            instance?.alertInProgress = false
            instance?.fallDetector?.triggerSimulatedFall(fallType)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            buildOngoingNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fallDetector = FallDetector(sensorManager) { _, _ -> onFallDetected() }
        fallDetector.start()
        Log.d(TAG, "Fall detection service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        fallDetector.stop()
        vibrator.cancel()
        scope.cancel()
        Log.d(TAG, "Fall detection service stopped")
    }

    private fun onFallDetected() {
        val now = System.currentTimeMillis()
        if (alertInProgress || (now - lastAlertTime < COOLDOWN_MS && lastAlertTime != 0L)) return

        alertInProgress = true
        lastAlertTime = now
        Log.d(TAG, "Fall detected — vibrating and sending to phone")

        // Vigorous SOS vibration
        vibrateAlert()

        // Send to phone — phone handles the prompt and API call
        scope.launch {
            val sent = FallMessageSender.sendToPhone(applicationContext)
            if (sent) {
                Log.d(TAG, "Fall alert sent to phone")
            } else {
                Log.e(TAG, "Failed to reach phone")
            }
            // Stop vibration after a few seconds
            kotlinx.coroutines.delay(4_000)
            vibrator.cancel()
            alertInProgress = false
        }
    }

    private fun vibrateAlert() {
        val pattern = longArrayOf(
            0, 200, 100, 200, 100, 200,   // S: ...
            200, 500, 100, 500, 100, 500,  // O: ---
            200, 200, 100, 200, 100, 200   // S: ...
        )
        val amplitudes = intArrayOf(
            0, 255, 0, 255, 0, 255,
            0, 255, 0, 255, 0, 255,
            0, 255, 0, 255, 0, 255
        )
        val effect = VibrationEffect.createWaveform(pattern, amplitudes, 0)
        vibrator.vibrate(effect)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Fall Detection", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Ongoing fall detection monitoring"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildOngoingNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Fall Detection Active")
            .setContentText("Monitoring for falls")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }
}
