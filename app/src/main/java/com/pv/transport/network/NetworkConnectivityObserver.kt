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
import java.util.concurrent.ConcurrentHashMap
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
    private val networkCaps = ConcurrentHashMap<Network, NetworkCapabilities>()
    private val _status = MutableStateFlow(getCurrentStatus())
    private var offlineEmitJob: Job? = null

    init {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                DebugLog.d("CALLBACK", "onAvailable")
                connectivityManager.getNetworkCapabilities(network)?.let { networkCaps[network] = it }
                evaluate()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                networkCaps[network] = networkCapabilities
                evaluate()
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                // Wifi/data handover often fires this. Do not flip UI; re-check remaining networks.
                evaluate()
            }

            override fun onLost(network: Network) {
                DebugLog.d("CALLBACK", "onLost")
                networkCaps.remove(network)
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
        return if (hasRealInternet()) {
            ConnectivityObserver.Status.Available
        } else {
            ConnectivityObserver.Status.Unavailable
        }
    }

    private fun hasRealInternet(): Boolean {
        if (networkCaps.values.any { NetworkUtils.hasValidatedInternet(it) }) return true
        return NetworkUtils.hasValidatedInternet(connectivityManager)
    }

    private fun evaluate() {
        val next = if (hasRealInternet()) {
            ConnectivityObserver.Status.Available
        } else {
            ConnectivityObserver.Status.Unavailable
        }
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

        // Stay online across wifi/data radio switches; go offline only if still no real internet.
        if (_status.value == ConnectivityObserver.Status.Available) {
            if (offlineEmitJob?.isActive != true) {
                offlineEmitJob = scope.launch {
                    delay(OFFLINE_GRACE_MS)
                    if (!hasRealInternet()) {
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
        private const val OFFLINE_GRACE_MS = 4000L
    }
}
