package com.identityx.dashboard

data class DashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // User profile
    val userName: String = "",
    val userEmail: String = "",

    // Energy metrics
    val solarOutputKw: Double = 0.0,
    val batterySocPercent: Int = 0,
    val peakTariffRate: String = "--",

    // Connectivity / sync
    val isOfflineMode: Boolean = false,
    val pendingSyncCount: Int = 0,

    // Gemini AI agent
    val isGeneratingDispatch: Boolean = false,
    val geminiRecommendation: String? = null
)
