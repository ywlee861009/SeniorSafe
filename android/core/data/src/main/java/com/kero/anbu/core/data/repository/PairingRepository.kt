package com.kero.anbu.core.data.repository

import com.kero.anbu.core.model.*
import com.kero.anbu.core.network.ApiService
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
        api.disconnectPairing(DisconnectPairingRequest(pairingId))
}
