package com.pv.transport.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEvents {
    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent = _logoutEvent.asSharedFlow()

    private val _sessionDataClearedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionDataClearedEvent = _sessionDataClearedEvent.asSharedFlow()

    fun triggerLogout() {
        _logoutEvent.tryEmit(Unit)
    }

    fun triggerSessionDataCleared() {
        _sessionDataClearedEvent.tryEmit(Unit)
    }
}