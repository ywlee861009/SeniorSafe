package com.seniorsafe.core.data.repository

import com.seniorsafe.core.model.*
import com.seniorsafe.core.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getPairingCode(): PairingCodeResponse =
        api.createPairingCode()

    suspend fun connectSenior(code: String): ClaimPairingResponse =
        api.claimPairingCode(ClaimPairingRequest(code))

    suspend fun getPairingList(): List<PairingItem> =
        api.getPairingList().pairings

    suspend fun disconnectPairing(pairingId: String): DisconnectPairingResponse =
        api.disconnectPairing(pairingId)
}
