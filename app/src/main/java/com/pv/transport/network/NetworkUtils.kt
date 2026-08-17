package com.pv.transport.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return hasValidatedInternet(cm)
    }

    /** True only when a network can actually reach the internet, not merely that radios are on. */
    fun hasValidatedInternet(connectivityManager: ConnectivityManager): Boolean {
        val active = connectivityManager.activeNetwork
        val activeCaps = active?.let { connectivityManager.getNetworkCapabilities(it) }
        if (activeCaps.isValidatedInternet()) return true

        @Suppress("DEPRECATION")
        return connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network).isValidatedInternet()
        }
    }

    fun hasValidatedInternet(capabilities: NetworkCapabilities?): Boolean =
        capabilities.isValidatedInternet()
}

private fun NetworkCapabilities?.isValidatedInternet(): Boolean {
    if (this == null) return false
    return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
