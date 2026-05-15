package com.seniorsafe.core.data.repository

import com.seniorsafe.core.datastore.TokenDataStore
import com.seniorsafe.core.model.AuthResponse
import com.seniorsafe.core.model.LoginRequest
import com.seniorsafe.core.model.RegisterRequest
import com.seniorsafe.core.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun login(email: String, password: String): AuthResponse {
        val res = api.login(LoginRequest(email, password))
        tokenDataStore.saveAuth(res.accessToken, res.userType, res.name, res.userId)
        return res
    }

    suspend fun register(email: String, password: String, name: String, phone: String, userType: String) {
        api.register(RegisterRequest(email, password, name, phone, userType))
    }

    suspend fun getToken(): String? = tokenDataStore.getAccessToken()

    suspend fun getBearerToken(): String = "Bearer ${tokenDataStore.getAccessToken()}"
}
