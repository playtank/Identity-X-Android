package com.identityx.authentication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.identityx.android.core.network.model.UserProfile
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

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { DashboardUiState.Loading }
            try {
                val profile = apiClient.getProfile()
                _uiState.update { DashboardUiState.Success(profile) }
            } catch (e: Exception) {
                _uiState.update { DashboardUiState.Error(e.message ?: "Failed to load profile") }
            }
        }
    }

    sealed interface DashboardUiState {
        data object Loading : DashboardUiState
        data class Success(val profile: UserProfile) : DashboardUiState
        data class Error(val message: String) : DashboardUiState
    }
}
