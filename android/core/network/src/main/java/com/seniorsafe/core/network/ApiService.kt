package com.seniorsafe.core.network

import com.seniorsafe.core.model.*
import retrofit2.http.*

interface ApiService {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): DeviceRegisterResponse

    @GET("devices/me")
    suspend fun getCurrentDevice(): DeviceMeResponse

    @PUT("devices/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): MessageResponse

    @POST("pairing/codes")
    suspend fun createPairingCode(): PairingCodeResponse

    @POST("pairings")
    suspend fun claimPairingCode(@Body request: ClaimPairingRequest): ClaimPairingResponse

    @GET("pairings")
    suspend fun getPairingList(): PairingListResponse

    @DELETE("pairings/{pairingId}")
    suspend fun disconnectPairing(@Path("pairingId") pairingId: String): DisconnectPairingResponse

    @POST("activity/events")
    suspend fun recordActivityEvent(@Body request: ActivityEventRequest): ActivityEventResponse

    @GET("activity/events/{seniorDeviceId}")
    suspend fun getActivityEvents(
        @Path("seniorDeviceId") seniorDeviceId: String,
        @Query("limit") limit: Int = 50
    ): ActivityEventsResponse

    @POST("activity/service-events")
    suspend fun recordServiceEvent(@Body request: ServiceEventRequest): ServiceEventResponse

    @GET("activity/service-events/{deviceId}")
    suspend fun getServiceEvents(@Path("deviceId") deviceId: String): ServiceEventsResponse

    @GET("activity/inactivity-alerts/{seniorDeviceId}")
    suspend fun getInactivityAlerts(
        @Path("seniorDeviceId") seniorDeviceId: String
    ): InactivityAlertsResponse
}
