package com.kero.anbu.core.network

import com.kero.anbu.core.model.*
import retrofit2.http.*

/**
 * Supabase Edge Functions client.
 *
 * Base URL is the Functions root: https://<project>.supabase.co/functions/v1/
 * Each path below is an Edge Function name (kebab-case). Endpoints that hit
 * PostgREST instead use an absolute path (leading "/") to bypass the Functions
 * prefix. See docs/api-spec.md for the full contract.
 */
interface ApiService {
    @POST("device-register")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): DeviceRegisterResponse

    // Current device profile via PostgREST (RLS returns only the caller's device).
    // Absolute path keeps the host but drops the /functions/v1 prefix.
    // NOTE: PostgREST returns an array; not currently called from UI.
    @GET("/rest/v1/devices")
    suspend fun getCurrentDevice(): DeviceMeResponse

    @PUT("fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): MessageResponse

    @POST("pairing-codes")
    suspend fun createPairingCode(): PairingCodeResponse

    @POST("pairing-claim")
    suspend fun claimPairingCode(@Body request: ClaimPairingRequest): ClaimPairingResponse

    @GET("pairings-list")
    suspend fun getPairingList(): PairingListResponse

    @POST("pairing-disconnect")
    suspend fun disconnectPairing(@Body request: DisconnectPairingRequest): DisconnectPairingResponse

    // TODO(ticket-004): backend `activity-events` expects a batch body
    // {events:[...]} and returns {accepted:N}. Align ActivityEventRequest/Response
    // to the batch contract when wiring senior activity upload. No caller yet.
    @POST("activity-events")
    suspend fun recordActivityEvent(@Body request: ActivityEventRequest): ActivityEventResponse

    @GET("activity-events-list")
    suspend fun getActivityEvents(
        @Query("senior_device_id") seniorDeviceId: String,
        @Query("limit") limit: Int = 50
    ): ActivityEventsResponse

    // TODO(ticket-004): backend `service-events` expects a batch body
    // {events:[...]} and returns {accepted:N}. No caller yet.
    @POST("service-events")
    suspend fun recordServiceEvent(@Body request: ServiceEventRequest): ServiceEventResponse

    @GET("service-events-list")
    suspend fun getServiceEvents(@Query("device_id") deviceId: String): ServiceEventsResponse

    @GET("inactivity-alerts-list")
    suspend fun getInactivityAlerts(
        @Query("senior_device_id") seniorDeviceId: String
    ): InactivityAlertsResponse
}
