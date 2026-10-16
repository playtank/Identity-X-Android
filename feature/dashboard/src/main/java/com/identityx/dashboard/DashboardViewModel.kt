package com.identityx.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.android.core.network.restful.IdentityXApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val apiClient: IdentityXApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profile = apiClient.getProfile()
                _uiState.update {
                    it.copy(
                        isLoading  = false,
                        userName   = profile.displayName,
                        userEmail  = profile.email
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    fun onGenerateAgentDispatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingDispatch = true) }
            // TODO: wire Gemini AI agent call
            kotlinx.coroutines.delay(2_000)
            _uiState.update {
                it.copy(
                    isGeneratingDispatch  = false,
                    geminiRecommendation  = "Optimise battery discharge during peak tariff 18:00–21:00. Solar surplus available for grid export."
                )
            }
        }
    }
}
