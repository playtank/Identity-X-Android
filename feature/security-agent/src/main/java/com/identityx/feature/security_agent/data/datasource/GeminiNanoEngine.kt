package com.identityx.feature.security_agent.data.datasource

import com.identityx.feature.security_agent.domain.agent.AgentSecurityAction
import com.identityx.feature.security_agent.domain.model.SecurityRiskLevel
import com.identityx.feature.security_agent.domain.model.TelemetryReport
import kotlinx.coroutines.delay

class GeminiNanoEngine {

    /**
     * Executes local telemetry evaluation.
     */
    suspend fun evaluateTelemetry(telemetry: TelemetryReport): AgentSecurityAction {
        // 模拟本地 NPU 推理耗时 (100ms)
        delay(100)

        return when {
            telemetry.isVpnActive -> {
                AgentSecurityAction.RequireBiometricAuth(
                    riskLevel = SecurityRiskLevel.HIGH,
                    reason = "Anomalous VPN tunnel detected during session execution."
                )
            }
            telemetry.deviceOrientation == "Shaking" -> {
                AgentSecurityAction.RequireBiometricAuth(
                    riskLevel = SecurityRiskLevel.MEDIUM,
                    reason = "Unstable physical device motion profile detected."
                )
            }
            else -> AgentSecurityAction.Allow
        }
    }
}