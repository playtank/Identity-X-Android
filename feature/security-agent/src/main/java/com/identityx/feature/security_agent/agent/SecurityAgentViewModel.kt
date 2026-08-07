package com.identityx.feature.security_agent.agent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.feature.security_agent.domain.usecase.EvaluateSessionRiskUseCase
import kotlinx.coroutines.launch

class SecurityAgentViewModel(
    private val evaluateSessionRiskUseCase: EvaluateSessionRiskUseCase
) : ViewModel() {

    fun runSecurityCheck() {
        viewModelScope.launch {
            val (riskLevel, reason) = evaluateSessionRiskUseCase.execute()
            Log.d("IdentityX_Agent", "Security Check Result -> Risk: $riskLevel | Reason: $reason")
        }
    }
}