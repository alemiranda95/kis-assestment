package com.assestment.kis.data.core.network

import com.assestment.kis.data.session.remote.SessionApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Builds the real Retrofit stack. The runtime app binds the in-memory fake remote (Option B),
 * so this is the documented production wiring and the target of the MockWebServer tests; point
 * [BASE_URL] at the real backend to go live.
 */
object NetworkFactory {

    private const val BASE_URL = "http://localhost/"

    private val json = Json { ignoreUnknownKeys = true }

    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    fun retrofit(client: OkHttpClient = okHttpClient()): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun sessionApi(retrofit: Retrofit = retrofit()): SessionApi = retrofit.create(SessionApi::class.java)
}
