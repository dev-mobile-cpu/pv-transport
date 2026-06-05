package com.pv.transport.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEvents {
    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent = _logoutEvent.asSharedFlow()

    fun triggerLogout() {
        _logoutEvent.tryEmit(Unit)
    }
}