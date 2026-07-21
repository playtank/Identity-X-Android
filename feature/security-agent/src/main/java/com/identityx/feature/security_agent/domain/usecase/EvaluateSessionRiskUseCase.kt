package com.identityx.feature.security_agent.domain.usecase

import com.identityx.feature.security_agent.domain.agent.AgentSecurityAction
import com.identityx.feature.security_agent.domain.agent.LocalAgentEngine
import com.identityx.feature.security_agent.domain.model.SecurityRiskLevel
import com.identityx.feature.security_agent.domain.repository.TelemetryRepository

class EvaluateSessionRiskUseCase(
    private val telemetryRepository: TelemetryRepository,
    private val agentEngine: LocalAgentEngine
) {
    suspend fun execute(): Pair<SecurityRiskLevel, String> {
        val telemetry = telemetryRepository.getCurrentTelemetry()

        return when (val action = agentEngine.decideNextAction(telemetry)) {
            is AgentSecurityAction.Allow -> {
                SecurityRiskLevel.LOW to "Environment verified safe."
            }
            is AgentSecurityAction.RequireBiometricAuth -> {
                action.riskLevel to action.reason
            }
            is AgentSecurityAction.TerminateSession -> {
                SecurityRiskLevel.HIGH to "Session terminated: ${action.cause}"
            }
        }
    }
}