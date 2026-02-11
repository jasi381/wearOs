package com.example.capabiltiesa

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FallAlertHandlerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timeoutJob: Job? = null

    companion object {
        private const val TAG = "FallAlertHandler"
        private const val CHANNEL_ID = "fall_alert_channel"
        private const val NOTIFICATION_ID = 2001
        private const val TIMEOUT_MS = 5_000L

        const val ACTION_START = "com.example.capabiltiesa.FALL_ALERT_START"
        const val ACTION_OK = "com.example.capabiltiesa.FALL_ALERT_OK"
        const val ACTION_HELP = "com.example.capabiltiesa.FALL_ALERT_HELP"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleFallAlert()
            ACTION_OK -> handleDismiss()
            ACTION_HELP -> handleHelp()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timeoutJob?.cancel()
        scope.cancel()
    }

    private fun handleFallAlert() {
        Log.d(TAG, "Showing fall alert notification on phone")
        startForeground(NOTIFICATION_ID, buildAlertNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

        // 5-second timeout
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(TIMEOUT_MS)
            Log.d(TAG, "No response in ${TIMEOUT_MS / 1000}s — calling API")
            callApiAndStop()
        }
    }

    private fun handleDismiss() {
        Log.d(TAG, "User tapped I'm OK — dismissing")
        timeoutJob?.cancel()
        cleanupAndStop()
    }

    private fun handleHelp() {
        Log.d(TAG, "User tapped Send Help — calling API")
        timeoutJob?.cancel()
        callApiAndStop()
    }

    private fun callApiAndStop() {
        scope.launch {
            val success = FallAlertApiCaller.sendAlert(source = "phone")
            if (success) {
                Log.d(TAG, "API alert sent successfully from phone")
            } else {
                Log.e(TAG, "Phone API alert failed")
            }
            cleanupAndStop()
        }
    }

    private fun cleanupAndStop() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildAlertNotification(): Notification {
        // "I'm OK" action
        val okIntent = Intent(this, FallAlertHandlerService::class.java).apply {
            action = ACTION_OK
        }
        val okPending = PendingIntent.getService(
            this, 1, okIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Send Help" action
        val helpIntent = Intent(this, FallAlertHandlerService::class.java).apply {
            action = ACTION_HELP
        }
        val helpPending = PendingIntent.getService(
            this, 2, helpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Fall Detected on Watch!")
            .setContentText("Sending help in 5 seconds unless you respond")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                Notification.Action.Builder(
                    null, "I'm OK", okPending
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    null, "Send Help", helpPending
                ).build()
            )
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Fall Alerts", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent fall detection alerts from watch"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
