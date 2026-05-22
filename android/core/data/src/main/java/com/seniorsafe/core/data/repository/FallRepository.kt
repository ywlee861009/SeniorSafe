package com.seniorsafe.core.data.repository

import com.seniorsafe.core.model.*
import com.seniorsafe.core.network.ApiService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FallRepository @Inject constructor(
    @Suppress("unused") private val api: ApiService
) {
    // Service → NavHost 이벤트 버스
    private val _fallDetectedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val fallDetectedEvent: SharedFlow<Unit> = _fallDetectedEvent.asSharedFlow()

    fun publishFallDetected() { _fallDetectedEvent.tryEmit(Unit) }

    suspend fun reportFall(detectedAt: String): FallEventResponse =
        error("낙상 API는 현재 MVP에서 보류되었습니다.")

    suspend fun cancelFall(eventId: String): Unit =
        error("낙상 API는 현재 MVP에서 보류되었습니다.")

    suspend fun getFallHistory(seniorId: String): List<FallHistoryItem> =
        emptyList()
}
