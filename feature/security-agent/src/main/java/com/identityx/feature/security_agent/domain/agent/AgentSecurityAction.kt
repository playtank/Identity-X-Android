package com.identityx.feature.security_agent.domain.agent

import com.identityx.feature.security_agent.domain.model.SecurityRiskLevel

sealed interface AgentSecurityAction {
    data object Allow : AgentSecurityAction

    data class RequireBiometricAuth(
        val riskLevel: SecurityRiskLevel,
        val reason: String
    ) : AgentSecurityAction

    data class TerminateSession(val cause: String) : AgentSecurityAction
}