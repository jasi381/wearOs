package com.kinected.kinectedcarereceiver.presentation.ui.dashboard.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

/**
 * Check Mobile App Capability
 *
 * Verifies if the companion mobile app is installed on the connected phone
 * by checking for the "fall_detection_mobile" capability.
 *
 * @param context Android context
 * @param onResult Callback with installation status
 */
fun checkMobileAppCapability(
    context: Context,
    onResult: (Boolean) -> Unit
) {
    val capabilityClient = Wearable.getCapabilityClient(context)
    capabilityClient.getCapability("fall_detection_mobile", CapabilityClient.FILTER_ALL)
        .addOnSuccessListener { capabilityInfo ->
            val hasApp = capabilityInfo.nodes.isNotEmpty()
            onResult(hasApp)
            Log.d(
                "FallGuardDashboard",
                "Mobile app capability check: $hasApp (${capabilityInfo.nodes.size} nodes)"
            )
        }
        .addOnFailureListener { exception ->
            Log.e("FallGuardDashboard", "Failed to check mobile app capability", exception)
            onResult(false)
        }
}

/**
 * Check Phone Connectivity
 *
 * Checks if the watch is connected to a phone via Bluetooth or Wi-Fi
 * using the Wearable API.
 *
 * @param context Android context
 * @param onResult Callback with connectivity status and node name
 */
fun checkPhoneConnectivity(
    context: Context,
    onResult: (isConnected: Boolean, nodeName: String) -> Unit
) {
    val nodeClient = Wearable.getNodeClient(context)
    nodeClient.connectedNodes.addOnSuccessListener { nodes ->
        val isConnected = nodes.isNotEmpty()
        val nodeName = if (nodes.isNotEmpty()) {
            nodes.first().displayName
        } else {
            ""
        }
        onResult(isConnected, nodeName)
    }.addOnFailureListener { exception ->
        Log.e("FallGuardDashboard", "Connectivity check failed", exception)
        onResult(false, "")
    }
}
