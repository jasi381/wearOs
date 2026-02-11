package com.example.capabiltiesa.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class FallAlertRequest(
    val source: String,
    val timestamp: Long
)

data class FallAlertResponse(
    val status: String? = null,
    val message: String? = null
)

interface FallAlertApi {

    @POST("api/fall-alert")
    suspend fun sendAlert(@Body request: FallAlertRequest): Response<FallAlertResponse>
}
