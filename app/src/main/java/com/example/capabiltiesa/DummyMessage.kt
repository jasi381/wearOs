package com.example.capabiltiesa

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class DummyMessage(
    val name: String,
    val photoUrl: String? = null,
    val timestamp: Long
) {
    fun toBytes(): ByteArray = Json.encodeToString(this).toByteArray()

    companion object {
        fun fromBytes(bytes: ByteArray): DummyMessage =
            Json.decodeFromString(String(bytes))
    }
}
