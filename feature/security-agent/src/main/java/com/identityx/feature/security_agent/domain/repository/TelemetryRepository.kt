package com.identityx.feature.security_agent.domain.repository

import com.identityx.feature.security_agent.domain.model.TelemetryReport

interface TelemetryRepository {
    suspend fun getCurrentTelemetry(): TelemetryReport
}