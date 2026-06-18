package com.assestment.kis.data.session.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionApi {

    @POST("sessions")
    suspend fun postSession(@Body session: SessionDto): Response<Unit>

    @GET("sessions")
    suspend fun getSessions(): Response<List<SessionDto>>

    @GET("session/{id}")
    suspend fun getSession(@Path("id") id: String): Response<SessionDto>
}
