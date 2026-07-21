package com.identityx.feature.security_agent.domain.agent

import com.identityx.feature.security_agent.domain.model.TelemetryReport

interface LocalAgentEngine {
    suspend fun decideNextAction(telemetry: TelemetryReport): AgentSecurityAction
}