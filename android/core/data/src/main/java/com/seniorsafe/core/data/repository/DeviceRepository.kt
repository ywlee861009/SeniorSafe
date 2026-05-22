package com.seniorsafe.core.data.repository

import com.seniorsafe.core.datastore.DeviceDataStore
import com.seniorsafe.core.datastore.TokenDataStore
import com.seniorsafe.core.model.DeviceMeResponse
import com.seniorsafe.core.model.DeviceRegisterRequest
import com.seniorsafe.core.model.DeviceRegisterResponse
import com.seniorsafe.core.model.DeviceRole
import com.seniorsafe.core.model.FcmTokenRequest
import com.seniorsafe.core.model.MessageResponse
import com.seniorsafe.core.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val api: ApiService,
    private val deviceDataStore: DeviceDataStore,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun registerCurrentDevice(
        role: DeviceRole,
        displayName: String = defaultDisplayName(role),
        fcmToken: String? = null
    ): DeviceRegisterResponse {
        deviceDataStore.saveRole(role)
        val installId = deviceDataStore.ensureLocalDeviceId()
        val response = api.registerDevice(
            DeviceRegisterRequest(
                installId = installId,
                role = role.value,
                displayName = displayName,
                fcmToken = fcmToken
            )
        )
        tokenDataStore.saveDeviceAuth(
            token = response.deviceAccessToken,
            deviceId = response.deviceId,
            role = response.role,
            displayName = response.displayName
        )
        return response
    }

    suspend fun getCurrentDevice(): DeviceMeResponse =
        api.getCurrentDevice()

    suspend fun updateFcmToken(fcmToken: String): MessageResponse =
        api.updateFcmToken(FcmTokenRequest(fcmToken))

    suspend fun hasDeviceToken(): Boolean =
        !tokenDataStore.getDeviceAccessToken().isNullOrBlank()

    private fun defaultDisplayName(role: DeviceRole): String =
        when (role) {
            DeviceRole.SENIOR -> "어르신"
            DeviceRole.GUARDIAN -> "보호자"
        }
}
