package com.seniorsafe.core.data.repository

import com.seniorsafe.core.datastore.TokenDataStore
import com.seniorsafe.core.model.AuthResponse
import com.seniorsafe.core.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @Suppress("unused") private val api: ApiService,
    private val deviceRepository: DeviceRepository,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun login(email: String, password: String): AuthResponse =
        error("로그인 API는 현재 MVP에서 사용하지 않습니다.")

    suspend fun register(email: String, password: String, name: String, phone: String, userType: String): Unit =
        error("회원가입 API는 현재 MVP에서 사용하지 않습니다.")

    suspend fun getToken(): String? = tokenDataStore.getAccessToken()

    suspend fun getBearerToken(): String = "Bearer ${tokenDataStore.getAccessToken()}"

    suspend fun updateFcmToken(fcmToken: String) {
        deviceRepository.updateFcmToken(fcmToken)
    }
}
