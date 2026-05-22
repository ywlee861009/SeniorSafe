package com.seniorsafe.core.model

import com.google.gson.annotations.SerializedName

// ─── Legacy Login Compatibility ──────────────────────────────────────────────

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("user_type") val userType: String,
    val name: String,
    @SerializedName("user_id") val userId: String
)

// ─── Backend Device MVP ──────────────────────────────────────────────────────

data class HealthResponse(val status: String)

data class DeviceRegisterRequest(
    @SerializedName("install_id") val installId: String,
    val role: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("fcm_token") val fcmToken: String? = null
)

data class DeviceRegisterResponse(
    @SerializedName("device_id") val deviceId: String,
    val role: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("device_access_token") val deviceAccessToken: String
)

data class DeviceMeResponse(
    @SerializedName("device_id") val deviceId: String,
    val role: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("last_seen_at") val lastSeenAt: String,
    @SerializedName("last_activity_at") val lastActivityAt: String? = null,
    @SerializedName("inactivity_threshold_days") val inactivityThresholdDays: Int? = null
)

data class FcmTokenRequest(@SerializedName("fcm_token") val fcmToken: String)

data class MessageResponse(val message: String)

// ─── Backend Pairing MVP ─────────────────────────────────────────────────────

data class PairingCodeResponse(
    val code: String,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int? = null
)

data class ClaimPairingRequest(val code: String)

data class ClaimPairingResponse(
    @SerializedName("pairing_id") val pairingId: String,
    @SerializedName("senior_device_id") val seniorDeviceId: String,
    @SerializedName("senior_display_name") val seniorDisplayName: String,
    @SerializedName("created_at") val createdAt: String
) {
    val seniorId: String get() = seniorDeviceId
    val seniorName: String get() = seniorDisplayName
}

data class PairingItem(
    @SerializedName("pairing_id") val pairingId: String,
    val active: Boolean,
    @SerializedName("senior_device_id") val seniorDeviceId: String? = null,
    @SerializedName("senior_display_name") val seniorDisplayName: String? = null,
    @SerializedName("guardian_device_id") val guardianDeviceId: String? = null,
    @SerializedName("guardian_display_name") val guardianDisplayName: String? = null,
    @SerializedName("last_seen_at") val lastSeenAt: String? = null,
    @SerializedName("last_activity_at") val lastActivityAt: String? = null,
    @SerializedName("inactivity_threshold_days") val inactivityThresholdDays: Int? = null,
    @SerializedName("last_fall_at") val lastFallAt: String? = null
) {
    val seniorId: String get() = seniorDeviceId.orEmpty()
    val seniorName: String get() = seniorDisplayName ?: guardianDisplayName ?: "연결된 기기"
    val serviceActive: Boolean get() = active
}

data class PairingListResponse(val pairings: List<PairingItem>)

data class DisconnectPairingResponse(
    @SerializedName("pairing_id") val pairingId: String,
    val active: Boolean
)

// ─── Planned Activity API Contract ───────────────────────────────────────────

data class ActivityEventRequest(
    @SerializedName("occurred_at") val occurredAt: String,
    val source: String = "user_present"
)

data class ActivityEventResponse(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("last_activity_at") val lastActivityAt: String
)

data class ActivityEventItem(
    val id: String,
    @SerializedName("occurred_at") val occurredAt: String,
    @SerializedName("received_at") val receivedAt: String,
    val source: String
)

data class ActivityEventsResponse(val events: List<ActivityEventItem>)

data class ServiceEventRequest(
    @SerializedName("event_type") val eventType: String,
    @SerializedName("occurred_at") val occurredAt: String,
    val detail: String? = null
)

data class ServiceEventResponse(@SerializedName("event_id") val eventId: String)

data class ServiceEventItem(
    val id: String,
    @SerializedName("event_type") val eventType: String,
    @SerializedName("occurred_at") val occurredAt: String,
    @SerializedName("received_at") val receivedAt: String,
    val detail: String?
)

data class ServiceEventsResponse(val events: List<ServiceEventItem>)

data class InactivityAlertItem(
    val id: String,
    @SerializedName("senior_device_id") val seniorDeviceId: String,
    @SerializedName("guardian_device_id") val guardianDeviceId: String,
    @SerializedName("threshold_days") val thresholdDays: Int,
    @SerializedName("last_activity_at") val lastActivityAt: String?,
    @SerializedName("sent_at") val sentAt: String,
    val status: String
)

data class InactivityAlertsResponse(val alerts: List<InactivityAlertItem>)

// ─── Deferred Fall Compatibility ─────────────────────────────────────────────

data class FallEventResponse(
    @SerializedName("event_id") val eventId: String,
    val status: String
)

data class FallHistoryItem(
    val id: String,
    @SerializedName("detected_at") val detectedAt: String,
    val cancelled: Boolean
)

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
