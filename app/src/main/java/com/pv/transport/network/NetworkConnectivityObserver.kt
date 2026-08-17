package com.pv.transport.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.pv.transport.util.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ConnectivityObserver {
    fun observe(): Flow<Status>
    fun getCurrentStatus(): Status

    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}

class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
): ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _status = MutableStateFlow(getCurrentStatus())
    private var offlineEmitJob: Job? = null

    init {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                DebugLog.d("CALLBACK", "onAvailable")
                evaluate()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                evaluate()
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                evaluate()
            }

            override fun onLost(network: Network) {
                DebugLog.d("CALLBACK", "onLost")
                evaluate()
            }

            override fun onUnavailable() {
                evaluate()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        evaluate()
    }

    override fun observe(): Flow<ConnectivityObserver.Status> = _status.asStateFlow()

    override fun getCurrentStatus(): ConnectivityObserver.Status {
        return if (NetworkUtils.hasValidatedInternet(connectivityManager)) {
            ConnectivityObserver.Status.Available
        } else {
            ConnectivityObserver.Status.Unavailable
        }
    }

    private fun evaluate() {
        val next = getCurrentStatus()
        scope.launch { applyStatus(next) }
    }

    private fun applyStatus(next: ConnectivityObserver.Status) {
        if (next == ConnectivityObserver.Status.Available) {
            offlineEmitJob?.cancel()
            offlineEmitJob = null
            if (_status.value != ConnectivityObserver.Status.Available) {
                _status.value = ConnectivityObserver.Status.Available
            }
            return
        }

        // Stay online during wifi/data handover; flip to offline only if still down after grace.
        if (_status.value == ConnectivityObserver.Status.Available) {
            if (offlineEmitJob?.isActive != true) {
                offlineEmitJob = scope.launch {
                    delay(OFFLINE_GRACE_MS)
                    if (getCurrentStatus() != ConnectivityObserver.Status.Available) {
                        _status.value = ConnectivityObserver.Status.Unavailable
                    }
                }
            }
            return
        }

        if (_status.value != next) {
            _status.value = next
        }
    }

    companion object {
        private const val OFFLINE_GRACE_MS = 1500L
    }
}
