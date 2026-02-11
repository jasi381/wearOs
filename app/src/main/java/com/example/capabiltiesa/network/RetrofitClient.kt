package com.example.capabiltiesa.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://example.com/"
    private const val TIMEOUT_SECONDS = 10L

    @Volatile
    private var fallAlertApi: FallAlertApi? = null

    fun getFallAlertApi(context: Context): FallAlertApi {
        return fallAlertApi ?: synchronized(this) {
            fallAlertApi ?: buildApi(context.applicationContext).also { fallAlertApi = it }
        }
    }

    private fun buildApi(context: Context): FallAlertApi {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(
                        ChuckerCollector(
                            context = context,
                            showNotification = true,
                            retentionPeriod = RetentionManager.Period.ONE_HOUR
                        )
                    )
                    .maxContentLength(250_000L)
                    .alwaysReadResponseBody(true)
                    .build()
            )
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FallAlertApi::class.java)
    }
}
