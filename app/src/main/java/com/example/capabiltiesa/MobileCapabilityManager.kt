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

    private val _wearInstalled = MutableStateFlow<Boolean?>(null)
    val wearInstalled: StateFlow<Boolean?> = _wearInstalled

    private var wearNodeId: String? = null

    fun start() {
        Log.d(TAG, "start() called – registering capability listener")

        capabilityClient.addListener(
            this,
            Uri.parse("wear://*/$CAPABILITY_NAME"),
            CapabilityClient.FILTER_REACHABLE
        )

        Log.d(TAG, "Capability listener registered")

        Log.d(TAG, "Checking initial capability: $CAPABILITY_NAME")

        capabilityClient
            .getCapability(
                CAPABILITY_NAME,
                CapabilityClient.FILTER_REACHABLE
            )
            .addOnSuccessListener { info ->
                updateNodes(info)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to fetch capability: $CAPABILITY_NAME",
                    exception
                )
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
        Log.d(
            TAG,
            "onCapabilityChanged() -> name=${info.name}, nodes=${info.nodes}"
        )

        if (info.name == CAPABILITY_NAME) {
            updateNodes(info)
        }
    }

    private fun updateNodes(info: CapabilityInfo) {
        val isInstalled = info.nodes.isNotEmpty()
        wearNodeId = info.nodes.firstOrNull()?.id
        _wearInstalled.value = isInstalled

        Log.i(
            TAG,
            "Nodes updated -> wearInstalled=$isInstalled, nodeId=$wearNodeId"
        )
    }
}
