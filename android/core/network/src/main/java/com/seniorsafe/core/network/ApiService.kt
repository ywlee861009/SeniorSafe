package com.seniorsafe.core.network

import com.seniorsafe.core.model.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("pairing/code")
    suspend fun getPairingCode(@Header("Authorization") token: String): PairingCodeResponse

    @POST("pairing/connect")
    suspend fun connectSenior(
        @Header("Authorization") token: String,
        @Body request: ConnectRequest
    ): ConnectResponse

    @GET("pairing/list")
    suspend fun getPairingList(@Header("Authorization") token: String): PairingListResponse

    @POST("fall/event")
    suspend fun reportFallEvent(
        @Header("Authorization") token: String,
        @Body request: FallEventRequest
    ): FallEventResponse

    @POST("fall/cancel")
    suspend fun cancelFallEvent(
        @Header("Authorization") token: String,
        @Body request: FallCancelRequest
    ): FallEventResponse

    @GET("fall/history/{seniorId}")
    suspend fun getFallHistory(
        @Header("Authorization") token: String,
        @Path("seniorId") seniorId: String
    ): FallHistoryResponse

    @PUT("devices/token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): MessageResponse
}
