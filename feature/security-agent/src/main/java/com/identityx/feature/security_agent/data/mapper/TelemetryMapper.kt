package com.identityx.feature.security_agent.data.mapper

import com.identityx.feature.security_agent.data.datasource.TelemetryRawData
import com.identityx.feature.security_agent.domain.model.TelemetryReport

object TelemetryMapper {

    fun toDomain(rawData: TelemetryRawData): TelemetryReport {
        return TelemetryReport(
            isVpnActive = rawData.isVpnActive,
            deviceOrientation = rawData.orientation,
            ipAddress = rawData.ipAddress,
            timestamp = rawData.timestamp
        )
    }
}