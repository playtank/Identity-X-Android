package com.identityx.feature.security_agent

import com.identityx.feature.security_agent.data.agent.AdaptiveMockAgentEngine
import com.identityx.feature.security_agent.data.datasource.GeminiNanoEngine
import com.identityx.feature.security_agent.domain.model.SecurityRiskLevel
import com.identityx.feature.security_agent.domain.model.TelemetryReport
import com.identityx.feature.security_agent.domain.repository.TelemetryRepository
import com.identityx.feature.security_agent.domain.usecase.EvaluateSessionRiskUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EvaluateSessionRiskUseCaseTest {

    private lateinit var useCase: EvaluateSessionRiskUseCase
    private lateinit var fakeTelemetryRepository: FakeTelemetryRepository

    @Before
    fun setUp() {
        fakeTelemetryRepository = FakeTelemetryRepository()
        val engine = GeminiNanoEngine()
        val agent = AdaptiveMockAgentEngine(engine)

        useCase = EvaluateSessionRiskUseCase(fakeTelemetryRepository, agent)
    }

    @Test
    fun `when VPN is active, evaluation returns HIGH risk`() = runTest {
        // Given
        fakeTelemetryRepository.emitReport(
            TelemetryReport(isVpnActive = true, deviceOrientation = "Stable", ipAddress = "10.0.0.1")
        )

        // When
        val (riskLevel, reason) = useCase.execute()

        // Then
        assertEquals(SecurityRiskLevel.HIGH, riskLevel)
        assert(reason.contains("VPN"))
    }

    @Test
    fun `when environment is clean, evaluation returns LOW risk`() = runTest {
        // Given
        fakeTelemetryRepository.emitReport(
            TelemetryReport(isVpnActive = false, deviceOrientation = "Stable", ipAddress = "192.168.1.1")
        )

        // When
        val (riskLevel, _) = useCase.execute()

        // Then
        assertEquals(SecurityRiskLevel.LOW, riskLevel)
    }
}

// Simple Fake for rapid unit testing without Android context
class FakeTelemetryRepository : TelemetryRepository {
    private var currentReport = TelemetryReport(isVpnActive = false, deviceOrientation = "Stable", ipAddress = "127.0.0.1")

    fun emitReport(report: TelemetryReport) {
        currentReport = report
    }

    override suspend fun getCurrentTelemetry(): TelemetryReport = currentReport
}