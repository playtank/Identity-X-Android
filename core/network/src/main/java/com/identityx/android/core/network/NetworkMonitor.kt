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
    @Volatile private var isCurrentConnectionValid: Boolean = true

    fun isCurrentlyConnected(): Boolean = isCurrentConnectionValid

    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isCurrentConnectionValid = true
                trySend(true)
            }
            override fun onLost(network: Network) {
                isCurrentConnectionValid = false
                trySend(false)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
