package com.seniorsafe.core.data.repository

import com.seniorsafe.core.datastore.TokenDataStore
import com.seniorsafe.core.model.*
import com.seniorsafe.core.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getPairingCode(): PairingCodeResponse =
        api.getPairingCode("Bearer ${tokenDataStore.getAccessToken()}")

    suspend fun connectSenior(code: String): ConnectResponse =
        api.connectSenior("Bearer ${tokenDataStore.getAccessToken()}", ConnectRequest(code))

    suspend fun getPairingList(): List<PairingItem> =
        api.getPairingList("Bearer ${tokenDataStore.getAccessToken()}").pairings
}
