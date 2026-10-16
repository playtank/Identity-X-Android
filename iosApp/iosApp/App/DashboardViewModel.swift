//
//  DashboardViewModel.swift
//  iosApp
//
//  Mirrors Android's DashboardViewModel.
//  Loads user profile via the KMP IdentityXApiClient and drives the
//  Gemini workflow agent stub.
//

import Foundation
import Combine
import SharedAuthDomain

@MainActor
final class DashboardViewModel: ObservableObject {

    // MARK: - UI State (mirrors DashboardUiState)

    struct UiState {
        var isLoading:            Bool    = false
        var errorMessage:         String? = nil

        // User profile
        var userName:             String  = ""
        var userEmail:            String  = ""

        // Energy metrics (static placeholders until real telemetry API is wired)
        var solarOutputKw:        Double  = 0.0
        var batterySocPercent:    Int     = 0
        var peakTariffRate:       String  = "--"

        // Connectivity / sync
        var isOfflineMode:        Bool    = false
        var pendingSyncCount:     Int     = 0

        // Gemini AI agent
        var isGeneratingDispatch: Bool    = false
        var geminiRecommendation: String? = nil
    }

    @Published private(set) var uiState = UiState()

    // MARK: - Dependencies

    private let apiClient: NetworkIdentityXApiClient

    // MARK: - Init

    init(apiClient: NetworkIdentityXApiClient) {
        self.apiClient = apiClient
        loadProfile()
    }

    // MARK: - Actions

    func loadProfile() {
        uiState.isLoading     = true
        uiState.errorMessage  = nil

        apiClient.getProfile { [weak self] profile, error in
            guard let self else { return }
            DispatchQueue.main.async {
                self.uiState.isLoading = false
                if let error {
                    self.uiState.errorMessage = error.localizedDescription
                } else if let profile {
                    self.uiState.userName  = profile.displayName
                    self.uiState.userEmail = profile.email
                }
            }
        }
    }

    func onGenerateAgentDispatch() {
        guard !uiState.isGeneratingDispatch else { return }
        uiState.isGeneratingDispatch = true

        // TODO: wire real Gemini AI agent call — mirrors Android stub
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [weak self] in
            self?.uiState.isGeneratingDispatch = false
            self?.uiState.geminiRecommendation =
                "Optimise battery discharge during peak tariff 18:00–21:00. Solar surplus available for grid export."
        }
    }
}
