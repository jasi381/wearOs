package com.example.wearrr.fall

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object FallMessageSender {

    private const val TAG = "FallMessageSender"
    private const val FALL_ALERT_PATH = "/fall_alert"
    private const val CAPABILITY_MOBILE = "capability_mobile"

    /**
     * Sends a fall alert message to the connected phone via MessageClient.
     * Returns true if the message was sent successfully, false otherwise.
     */
    suspend fun sendToPhone(context: Context): Boolean {
        return try {
            val capabilityInfo = Wearable.getCapabilityClient(context)
                .getCapability(CAPABILITY_MOBILE, CapabilityClient.FILTER_REACHABLE)
                .await()

            val phoneNode = capabilityInfo.nodes.firstOrNull { it.isNearby }
                ?: capabilityInfo.nodes.firstOrNull()

            if (phoneNode == null) {
                Log.w(TAG, "No reachable phone node found")
                return false
            }

            val result = Wearable.getMessageClient(context)
                .sendMessage(phoneNode.id, FALL_ALERT_PATH, byteArrayOf())
                .await()

            Log.d(TAG, "Message sent to phone, request id: $result")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send fall alert to phone", e)
            false
        }
    }
}
