package com.identityx.feature.security_agent.data.repository

import com.identityx.feature.security_agent.data.datasource.TelemetryCollector
import com.identityx.feature.security_agent.data.mapper.TelemetryMapper
import com.identityx.feature.security_agent.domain.model.TelemetryReport
import com.identityx.feature.security_agent.domain.repository.TelemetryRepository

class TelemetryRepositoryImpl(
    private val telemetryCollector: TelemetryCollector
) : TelemetryRepository {

    override suspend fun getCurrentTelemetry(): TelemetryReport {
        val rawData = telemetryCollector.collectCurrentTelemetry()
        return TelemetryMapper.toDomain(rawData)
    }
}