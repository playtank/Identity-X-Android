package com.identityx.android.core.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    private val connectivityManager: ConnectivityManager
) {
    // 1. Maintain a quick synchronous atomic flag
    @Volatile private var isCurrentConnectionValid: Boolean = true

    // 2. A quick getter function that Ktor can read instantly
    fun isCurrentlyConnected(): Boolean = isCurrentConnectionValid
    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isCurrentConnectionValid = true
                trySend(true) // Internet is alive!
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isCurrentConnectionValid = false
                trySend(false) // Internet dropped!
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Clean up the system listener automatically when nobody is collecting this Flow
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}