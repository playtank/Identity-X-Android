package com.identityx.feature.security_agent.domain.model

data class TelemetryReport(
    val isVpnActive: Boolean,
    val deviceOrientation: String,
    val ipAddress: String,
    val timestamp: Long = System.currentTimeMillis()
)