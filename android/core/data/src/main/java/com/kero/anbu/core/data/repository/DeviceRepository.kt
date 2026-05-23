package com.kero.anbu.core.data.repository

import com.kero.anbu.core.datastore.DeviceDataStore
import com.kero.anbu.core.datastore.TokenDataStore
import com.kero.anbu.core.model.DeviceMeResponse
import com.kero.anbu.core.model.DeviceRegisterRequest
import com.kero.anbu.core.model.DeviceRegisterResponse
import com.kero.anbu.core.model.DeviceRole
import com.kero.anbu.core.model.FcmTokenRequest
import com.kero.anbu.core.model.MessageResponse
import com.kero.anbu.core.network.ApiService
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
