package com.example.capabiltiesa

import android.content.Context
import android.util.Log
import com.example.capabiltiesa.network.FallAlertRequest
import com.example.capabiltiesa.network.RetrofitClient

object FallAlertApiCaller {

    private const val TAG = "FallAlertApiCaller"

    suspend fun sendAlert(context: Context, source: String = "phone"): Boolean {
        return try {
            val api = RetrofitClient.getFallAlertApi(context)
            val request = FallAlertRequest(
                source = source,
                timestamp = System.currentTimeMillis()
            )
            val response = api.sendAlert(request)
            Log.d(TAG, "API response: ${response.code()} ${response.message()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            false
        }
    }
}
