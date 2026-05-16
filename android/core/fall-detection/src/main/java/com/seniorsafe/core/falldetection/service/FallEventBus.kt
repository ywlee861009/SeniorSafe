package com.seniorsafe.core.falldetection.service

import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class FallEventBus @Inject constructor(
    private val diagnosticsLogStore: DiagnosticsLogStore
) {

    private val _fallDetectedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val fallDetectedEvent: SharedFlow<Unit> = _fallDetectedEvent.asSharedFlow()

    fun publishFallDetected() {
        diagnosticsLogStore.add("FallEventBus", "fall detected event published")
        _fallDetectedEvent.tryEmit(Unit)
    }
}
