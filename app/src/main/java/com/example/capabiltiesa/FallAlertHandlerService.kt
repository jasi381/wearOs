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
    private var alertTimestamp: Long = 0L

    companion object {
        private const val TAG = "FallAlertHandler"
        private const val CHANNEL_ID = "fall_alert_channel"
        private const val NOTIFICATION_ID = 2001
        // Backup timeout — longer than the dialog's 15s countdown so it doesn't race
        private const val TIMEOUT_MS = 30_000L

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
            ACTION_START -> {
                alertTimestamp = intent.getLongExtra(
                    MainActivity.EXTRA_FALL_TIMESTAMP, System.currentTimeMillis()
                )
                handleFallAlert()
            }
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
        Log.d(TAG, "Posting fall alert notification")
        startForeground(NOTIFICATION_ID, buildAlertNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

        // Backup timeout — only fires if neither the dialog nor notification actions respond
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(TIMEOUT_MS)
            Log.d(TAG, "Backup timeout — no response in ${TIMEOUT_MS / 1000}s, calling API")
            callApiAndStop()
        }
    }

    private fun handleDismiss() {
        Log.d(TAG, "User tapped I'm OK — dismissing")
        timeoutJob?.cancel()
        cleanupAndStop()
    }

    private fun handleHelp() {
        Log.d(TAG, "User needs help — calling API")
        timeoutJob?.cancel()
        callApiAndStop()
    }

    private fun callApiAndStop() {
        scope.launch {
            val success = FallAlertApiCaller.sendAlert(context = applicationContext, source = "phone")
            if (success) {
                Log.d(TAG, "API alert sent successfully")
            } else {
                Log.e(TAG, "API alert failed")
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
        // Full-screen intent — opens MainActivity with fall data when phone is locked
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_FALL_DETECTED, true)
            putExtra(MainActivity.EXTRA_FALL_TIMESTAMP, alertTimestamp)
        }
        val fullScreenPending = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "I'm OK" action
        val okIntent = Intent(this, FallAlertHandlerService::class.java).apply {
            action = ACTION_OK
        }
        val okPending = PendingIntent.getService(
            this, 1, okIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Need Help" action
        val helpIntent = Intent(this, FallAlertHandlerService::class.java).apply {
            action = ACTION_HELP
        }
        val helpPending = PendingIntent.getService(
            this, 2, helpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Fall Detected on Watch!")
            .setContentText("Tap to open or respond below")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPending, true)
            .setContentIntent(fullScreenPending)
            .addAction(
                Notification.Action.Builder(
                    null, "I'm OK", okPending
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    null, "Need Help", helpPending
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
