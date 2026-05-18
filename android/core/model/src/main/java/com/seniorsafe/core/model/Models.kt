package com.seniorsafe.core.model

import com.google.gson.annotations.SerializedName

// ─── Auth ─────────────────────────────────────────────────────────────────────

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    @SerializedName("user_type") val userType: String
)

data class LoginRequest(val email: String, val password: String)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("user_type")    val userType: String,
    val name: String,
    @SerializedName("user_id")      val userId: String
)

// ─── Pairing ──────────────────────────────────────────────────────────────────

data class PairingCodeResponse(
    val code: String,
    @SerializedName("expires_at") val expiresAt: String
)

data class ConnectRequest(val code: String)

data class ConnectResponse(
    @SerializedName("senior_id")   val seniorId: String,
    @SerializedName("senior_name") val seniorName: String
)

data class PairingItem(
    @SerializedName("senior_id")      val seniorId: String,
    @SerializedName("senior_name")    val seniorName: String,
    @SerializedName("service_active") val serviceActive: Boolean,
    @SerializedName("last_fall_at")   val lastFallAt: String?
)

data class PairingListResponse(val pairings: List<PairingItem>)

// ─── Fall ─────────────────────────────────────────────────────────────────────

data class FallEventRequest(@SerializedName("detected_at") val detectedAt: String)

data class FallCancelRequest(@SerializedName("event_id") val eventId: String)

data class FallEventResponse(
    @SerializedName("event_id") val eventId: String,
    val status: String
)

data class FallHistoryItem(
    val id: String,
    @SerializedName("detected_at") val detectedAt: String,
    val cancelled: Boolean
)

data class FallHistoryResponse(val events: List<FallHistoryItem>)

// ─── Device ───────────────────────────────────────────────────────────────────

data class FcmTokenRequest(@SerializedName("fcm_token") val fcmToken: String)

data class MessageResponse(val message: String)

// ─── Local Device MVP ────────────────────────────────────────────────────────

enum class DeviceRole(val value: String) {
    SENIOR("senior"),
    GUARDIAN("guardian");

    companion object {
        fun fromValue(value: String?): DeviceRole? =
            entries.firstOrNull { it.value == value }
    }
}

enum class PairingStatus(val value: String) {
    UNPAIRED("unpaired"),
    PAIRED("paired");

    companion object {
        fun fromValue(value: String?): PairingStatus =
            entries.firstOrNull { it.value == value } ?: UNPAIRED
    }
}

data class LocalDeviceState(
    val localDeviceId: String?,
    val role: DeviceRole?,
    val pairingStatus: PairingStatus
)

data class DeviceRegistrationDraft(
    val localDeviceId: String,
    val role: DeviceRole
)
