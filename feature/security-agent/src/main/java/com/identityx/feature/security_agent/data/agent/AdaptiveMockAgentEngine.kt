package com.identityx.feature.security_agent.data.agent

import com.identityx.feature.security_agent.domain.agent.AgentSecurityAction
import com.identityx.feature.security_agent.domain.agent.LocalAgentEngine
import com.identityx.feature.security_agent.domain.model.SecurityRiskLevel
import com.identityx.feature.security_agent.domain.model.TelemetryReport
import kotlinx.coroutines.delay

class AdaptiveMockAgentEngine : LocalAgentEngine {
    override suspend fun decideNextAction(telemetry: TelemetryReport): AgentSecurityAction {
        delay(100) // 模拟轻量级推理延迟

        return when {
            telemetry.isVpnActive -> {
                AgentSecurityAction.RequireBiometricAuth(
                    riskLevel = SecurityRiskLevel.HIGH,
                    reason = "Anomalous active VPN tunnel detected."
                )
            }
            telemetry.deviceOrientation == "Shaking" -> {
                AgentSecurityAction.RequireBiometricAuth(
                    riskLevel = SecurityRiskLevel.MEDIUM,
                    reason = "Unstable physical device telemetry profile."
                )
            }
            else -> AgentSecurityAction.Allow
        }
    }
}