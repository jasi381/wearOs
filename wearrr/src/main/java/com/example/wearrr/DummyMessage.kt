package com.example.wearrr

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DummyMessage(
    val name: String,
    val photoUrl: String? = null,
    val timestamp: Long
) {
    companion object {
        fun fromBytes(bytes: ByteArray): DummyMessage =
            Json.decodeFromString(String(bytes))
    }
}
