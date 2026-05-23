package com.kero.anbu.core.data.repository

import com.kero.anbu.core.datastore.TokenDataStore
import com.kero.anbu.core.model.AuthResponse
import com.kero.anbu.core.network.ApiService
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @Suppress("unused") private val api: ApiService,
    private val deviceRepository: DeviceRepository,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun login(email: String, password: String): AuthResponse {
        val userType = tokenDataStore.getUserType() ?: "senior"
        val name = tokenDataStore.getUserName() ?: "테스트 사용자"
        val userId = "mock-user-${UUID.randomUUID()}"
        val response = AuthResponse(
            accessToken = "mock-token-${UUID.randomUUID()}",
            userType = userType,
            name = name,
            userId = userId
        )
        tokenDataStore.saveAuth(
            token = response.accessToken,
            userType = response.userType,
            name = response.name,
            userId = response.userId
        )
        return response
    }

    suspend fun register(email: String, password: String, name: String, phone: String, userType: String) {
        tokenDataStore.saveAuth(
            token = "mock-token-${UUID.randomUUID()}",
            userType = userType,
            name = name,
            userId = "mock-user-${UUID.randomUUID()}"
        )
    }

    suspend fun getToken(): String? = tokenDataStore.getAccessToken()

    suspend fun getBearerToken(): String = "Bearer ${tokenDataStore.getAccessToken()}"

    suspend fun updateFcmToken(fcmToken: String) {
        deviceRepository.updateFcmToken(fcmToken)
    }
}
