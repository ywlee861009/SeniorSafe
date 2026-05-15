package com.seniorsafe.feature.mvp.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MvpFallEventBus @Inject constructor() {

    private val _fallDetectedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val fallDetectedEvent: SharedFlow<Unit> = _fallDetectedEvent.asSharedFlow()

    fun publishFallDetected() {
        _fallDetectedEvent.tryEmit(Unit)
    }
}
