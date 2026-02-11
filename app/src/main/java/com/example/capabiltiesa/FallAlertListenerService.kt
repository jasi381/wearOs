package com.example.capabiltiesa

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class FallAlertListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "FallAlertListener"
        private const val FALL_ALERT_PATH = "/fall_alert"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == FALL_ALERT_PATH) {
            Log.d(TAG, "Received fall alert from watch")

            val timestamp = System.currentTimeMillis()

            // Launch MainActivity directly with fall data
            try {
                val activityIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(MainActivity.EXTRA_FALL_DETECTED, true)
                    putExtra(MainActivity.EXTRA_FALL_TIMESTAMP, timestamp)
                }
                startActivity(activityIntent)
                Log.d(TAG, "Launched MainActivity with fall intent")
            } catch (e: Exception) {
                Log.e(TAG, "Direct activity launch failed", e)
            }

            // Also start the handler service — posts notification as backup
            val serviceIntent = Intent(this, FallAlertHandlerService::class.java).apply {
                action = FallAlertHandlerService.ACTION_START
                putExtra(MainActivity.EXTRA_FALL_TIMESTAMP, timestamp)
            }
            startForegroundService(serviceIntent)
        }
    }
}
