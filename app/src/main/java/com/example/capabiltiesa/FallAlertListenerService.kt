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
            Log.d(TAG, "Received fall alert from watch — launching handler")
            val intent = Intent(this, FallAlertHandlerService::class.java).apply {
                action = FallAlertHandlerService.ACTION_START
            }
            startForegroundService(intent)
        }
    }
}
