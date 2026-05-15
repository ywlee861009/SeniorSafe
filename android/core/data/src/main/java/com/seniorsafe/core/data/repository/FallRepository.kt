package com.seniorsafe.core.data.repository

import com.seniorsafe.core.datastore.TokenDataStore
import com.seniorsafe.core.model.*
import com.seniorsafe.core.network.ApiService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FallRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore
) {
    // Service → NavHost 이벤트 버스
    private val _fallDetectedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val fallDetectedEvent: SharedFlow<Unit> = _fallDetectedEvent.asSharedFlow()

    fun publishFallDetected() { _fallDetectedEvent.tryEmit(Unit) }

    suspend fun reportFall(detectedAt: String): FallEventResponse =
        api.reportFallEvent("Bearer ${tokenDataStore.getAccessToken()}", FallEventRequest(detectedAt))

    suspend fun cancelFall(eventId: String) =
        api.cancelFallEvent("Bearer ${tokenDataStore.getAccessToken()}", FallCancelRequest(eventId))

    suspend fun getFallHistory(seniorId: String): List<FallHistoryItem> =
        api.getFallHistory("Bearer ${tokenDataStore.getAccessToken()}", seniorId).events
}
