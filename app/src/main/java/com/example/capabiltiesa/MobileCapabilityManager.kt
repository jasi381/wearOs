package com.example.capabiltiesa

import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log


class MobileCapabilityManager(
    context: Context
) : CapabilityClient.OnCapabilityChangedListener {

    companion object {
        private const val TAG = "MobileCapabilityMgr"
        private const val CAPABILITY_NAME = "capability_wear"
        const val MESSAGE_PATH = "/dummy_text"
    }

    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val messageClient = Wearable.getMessageClient(context)

    private val _peerInstalled = MutableStateFlow<Boolean?>(null)
    val peerInstalled: StateFlow<Boolean?> = _peerInstalled

    private val _peerReachable = MutableStateFlow<Boolean?>(null)
    val peerReachable: StateFlow<Boolean?> = _peerReachable

    private var wearNodeId: String? = null

    fun start() {
        Log.d(TAG, "start() called – registering capability listener")

        capabilityClient.addListener(
            this,
            Uri.parse("wear://*/$CAPABILITY_NAME"),
            CapabilityClient.FILTER_REACHABLE
        )

        Log.d(TAG, "Capability listener registered")

        // Check if app is installed anywhere (even if not reachable right now)
        capabilityClient
            .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_ALL)
            .addOnSuccessListener { info ->
                _peerInstalled.value = info.nodes.isNotEmpty()
                Log.d(TAG, "FILTER_ALL -> installed=${info.nodes.isNotEmpty()}, nodes=${info.nodes}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch FILTER_ALL capability", e)
            }

        // Check if app is currently reachable (installed + connected)
        capabilityClient
            .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { info ->
                updateNodes(info)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch FILTER_REACHABLE capability", e)
            }
    }

    fun stop() {
        Log.d(TAG, "stop() called – removing capability listener")
        capabilityClient.removeListener(this)
        Log.d(TAG, "Capability listener removed")
    }

    fun sendMessage(message: DummyMessage) {
        val nodeId = wearNodeId
        if (nodeId == null) {
            Log.w(TAG, "sendMessage() – no wear node connected")
            return
        }

        Log.d(TAG, "Sending message to node $nodeId: $message")

        messageClient.sendMessage(nodeId, MESSAGE_PATH, message.toBytes())
            .addOnSuccessListener {
                Log.i(TAG, "Message sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send message", e)
            }
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged() -> name=${info.name}, nodes=${info.nodes}")

        if (info.name == CAPABILITY_NAME) {
            updateNodes(info)
        }
    }

    private fun updateNodes(info: CapabilityInfo) {
        val isReachable = info.nodes.isNotEmpty()
        wearNodeId = info.nodes.firstOrNull()?.id
        _peerReachable.value = isReachable

        if (isReachable) {
            _peerInstalled.value = true
        } else {
            // Reachable went false — re-check FILTER_ALL to distinguish uninstall vs disconnect
            capabilityClient
                .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_ALL)
                .addOnSuccessListener { allInfo ->
                    _peerInstalled.value = allInfo.nodes.isNotEmpty()
                    Log.d(TAG, "Re-check FILTER_ALL -> installed=${allInfo.nodes.isNotEmpty()}")
                }
        }

        Log.i(TAG, "Nodes updated -> reachable=$isReachable, nodeId=$wearNodeId")
    }
}
