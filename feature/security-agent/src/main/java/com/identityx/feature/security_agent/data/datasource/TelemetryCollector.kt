package com.identityx.feature.security_agent.data.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class TelemetryCollector(private val context: Context) {

    fun collectCurrentTelemetry(): TelemetryRawData {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)

        // 检测系统底层 VPN 状态
        val isVpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false

        return TelemetryRawData(
            isVpnActive = isVpn,
            orientation = "Stable", // 可扩展 Android SensorManager 采样
            ipAddress = "192.168.1.100",
            timestamp = System.currentTimeMillis()
        )
    }
}

data class TelemetryRawData(
    val isVpnActive: Boolean,
    val orientation: String,
    val ipAddress: String,
    val timestamp: Long
)