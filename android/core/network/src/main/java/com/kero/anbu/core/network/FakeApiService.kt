package com.kero.anbu.core.network

import com.kero.anbu.core.model.ActivityEventItem
import com.kero.anbu.core.model.ActivityEventRequest
import com.kero.anbu.core.model.ActivityEventResponse
import com.kero.anbu.core.model.ActivityEventsResponse
import com.kero.anbu.core.model.ClaimPairingRequest
import com.kero.anbu.core.model.ClaimPairingResponse
import com.kero.anbu.core.model.DeviceMeResponse
import com.kero.anbu.core.model.DeviceRegisterRequest
import com.kero.anbu.core.model.DeviceRegisterResponse
import com.kero.anbu.core.model.DisconnectPairingResponse
import com.kero.anbu.core.model.FcmTokenRequest
import com.kero.anbu.core.model.HealthResponse
import com.kero.anbu.core.model.InactivityAlertsResponse
import com.kero.anbu.core.model.MessageResponse
import com.kero.anbu.core.model.PairingCodeResponse
import com.kero.anbu.core.model.PairingItem
import com.kero.anbu.core.model.PairingListResponse
import com.kero.anbu.core.model.ServiceEventItem
import com.kero.anbu.core.model.ServiceEventRequest
import com.kero.anbu.core.model.ServiceEventResponse
import com.kero.anbu.core.model.ServiceEventsResponse
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeApiService @Inject constructor() : ApiService {

    private var registeredDevice: DeviceRegisterResponse? = null
    private var activePairing: PairingItem? = null

    override suspend fun health(): HealthResponse =
        HealthResponse(status = "ok")

    override suspend fun registerDevice(request: DeviceRegisterRequest): DeviceRegisterResponse {
        val response = DeviceRegisterResponse(
            deviceId = "mock-device-${request.installId.take(8)}",
            role = request.role,
            displayName = request.displayName,
            deviceAccessToken = "mock-token-${UUID.randomUUID()}"
        )
        registeredDevice = response
        return response
    }

    override suspend fun getCurrentDevice(): DeviceMeResponse {
        val now = now()
        val device = registeredDevice
        return DeviceMeResponse(
            deviceId = device?.deviceId ?: "mock-device",
            role = device?.role ?: "senior",
            displayName = device?.displayName ?: "어르신",
            createdAt = now,
            lastSeenAt = now,
            lastActivityAt = now,
            inactivityThresholdDays = 3
        )
    }

    override suspend fun updateFcmToken(request: FcmTokenRequest): MessageResponse =
        MessageResponse(message = "ok")

    override suspend fun createPairingCode(): PairingCodeResponse =
        PairingCodeResponse(
            code = "123456",
            expiresAt = now(),
            expiresInSeconds = 600
        )

    override suspend fun claimPairingCode(request: ClaimPairingRequest): ClaimPairingResponse {
        val response = ClaimPairingResponse(
            pairingId = "mock-pairing",
            seniorDeviceId = "mock-senior-device",
            seniorDisplayName = "어르신",
            createdAt = now()
        )
        activePairing = response.toPairingItem()
        return response
    }

    override suspend fun getPairingList(): PairingListResponse {
        val pairing = activePairing ?: PairingItem(
            pairingId = "mock-pairing",
            active = true,
            seniorDeviceId = "mock-senior-device",
            seniorDisplayName = "어르신",
            guardianDeviceId = "mock-guardian-device",
            guardianDisplayName = "보호자",
            lastSeenAt = now(),
            lastActivityAt = now(),
            inactivityThresholdDays = 3
        )
        return PairingListResponse(pairings = listOf(pairing))
    }

    override suspend fun disconnectPairing(pairingId: String): DisconnectPairingResponse {
        activePairing = activePairing?.copy(active = false)
        return DisconnectPairingResponse(pairingId = pairingId, active = false)
    }

    override suspend fun recordActivityEvent(request: ActivityEventRequest): ActivityEventResponse =
        ActivityEventResponse(
            eventId = "mock-activity-${UUID.randomUUID()}",
            lastActivityAt = request.occurredAt
        )

    override suspend fun getActivityEvents(
        seniorDeviceId: String,
        limit: Int
    ): ActivityEventsResponse =
        ActivityEventsResponse(
            events = listOf(
                ActivityEventItem(
                    id = "mock-activity",
                    occurredAt = now(),
                    receivedAt = now(),
                    source = "user_present"
                )
            )
        )

    override suspend fun recordServiceEvent(request: ServiceEventRequest): ServiceEventResponse =
        ServiceEventResponse(eventId = "mock-service-${UUID.randomUUID()}")

    override suspend fun getServiceEvents(deviceId: String): ServiceEventsResponse =
        ServiceEventsResponse(
            events = listOf(
                ServiceEventItem(
                    id = "mock-service",
                    eventType = "mock_success",
                    occurredAt = now(),
                    receivedAt = now(),
                    detail = "백엔드 없이 성공 처리됨"
                )
            )
        )

    override suspend fun getInactivityAlerts(seniorDeviceId: String): InactivityAlertsResponse =
        InactivityAlertsResponse(alerts = emptyList())

    private fun ClaimPairingResponse.toPairingItem(): PairingItem =
        PairingItem(
            pairingId = pairingId,
            active = true,
            seniorDeviceId = seniorDeviceId,
            seniorDisplayName = seniorDisplayName,
            guardianDeviceId = "mock-guardian-device",
            guardianDisplayName = "보호자",
            lastSeenAt = now(),
            lastActivityAt = now(),
            inactivityThresholdDays = 3
        )

    private fun now(): String = Instant.now().toString()
}
