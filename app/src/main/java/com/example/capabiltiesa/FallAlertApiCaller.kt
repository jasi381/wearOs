package com.example.capabiltiesa

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object FallAlertApiCaller {

    private const val ALERT_URL = "https://example.com/api/fall-alert"
    private const val TIMEOUT_MS = 10_000

    /**
     * POST a fall alert to the backend. Returns true on HTTP 2xx, false otherwise.
     */
    suspend fun sendAlert(source: String = "phone"): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(ALERT_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.doOutput = true

            val body = """{"source":"$source","timestamp":${System.currentTimeMillis()}}"""
            conn.outputStream.use { it.write(body.toByteArray()) }

            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }
}
