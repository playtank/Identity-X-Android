//
//  DashboardView.swift
//  iosApp
//
//  Mirrors Android's DashboardScreen / DashboardScreenContent.
//
//  Layout:
//    NavigationStack
//      └── ScrollView (2-column LazyVGrid)
//            ├── CategorySummaryHeader   (full width)
//            ├── AiAgentCard             (full width)
//            ├── Solar Output card
//            ├── Battery Level card
//            ├── Grid Rate card
//            └── Sync Queue card
//

import SwiftUI

struct DashboardView: View {

    @EnvironmentObject private var authSession: AuthSession
    @ObservedObject var viewModel: DashboardViewModel

    @State private var showUserMenu = false

    private var state: DashboardViewModel.UiState { viewModel.uiState }

    // MARK: - Body

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVGrid(
                    columns: [GridItem(.flexible()), GridItem(.flexible())],
                    spacing: 12
                ) {
                    // Full-width summary header
                    CategorySummaryHeader(state: state)
                        .gridCellColumns(2)

                    // Full-width AI agent card
                    AiAgentCard(
                        state:           state,
                        onGenerateClick: viewModel.onGenerateAgentDispatch
                    )
                    .gridCellColumns(2)

                    // 2-column metric cards
                    MetricCard(
                        title:    "Solar Output",
                        value:    "\(String(format: "%.1f", state.solarOutputKw)) kW",
                        subtitle: "Generation Active",
                        icon:     "sun.max.fill",
                        iconColor: Color.orange
                    )

                    MetricCard(
                        title:    "Battery Level",
                        value:    "\(state.batterySocPercent)%",
                        subtitle: "15.1 kWh Remaining",
                        icon:     "bolt.fill",
                        iconColor: Color.green
                    )

                    MetricCard(
                        title:    "Grid Rate",
                        value:    state.peakTariffRate,
                        subtitle: "Off-Peak Tariff",
                        icon:     "arrow.2.circlepath",
                        iconColor: Color.blue
                    )

                    MetricCard(
                        title:    "Sync Queue",
                        value:    "\(state.pendingSyncCount) Items",
                        subtitle: state.isOfflineMode ? "Offline Stored" : "Synced",
                        icon:     state.isOfflineMode ? "wifi.slash" : "checkmark.icloud.fill",
                        iconColor: state.isOfflineMode ? .red : .green
                    )
                }
                .padding(16)
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                // Leading — app title + connectivity subtitle
                ToolbarItem(placement: .navigationBarLeading) {
                    VStack(alignment: .leading, spacing: 1) {
                        Text("Identity-X Energy")
                            .font(.headline)
                        Text(state.isOfflineMode
                             ? "Offline Mode (\(state.pendingSyncCount) pending)"
                             : "Online")
                            .font(.caption)
                            .foregroundStyle(state.isOfflineMode ? .red : Color(red: 0.18, green: 0.49, blue: 0.20))
                    }
                }

                // Trailing — user profile menu
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        // User info header (non-interactive)
                        Text(state.userName.isEmpty ? "Loading…" : state.userName)
                            .font(.headline)
                        Text(state.userEmail)
                            .font(.caption)
                            .foregroundStyle(.secondary)

                        Divider()

                        Button(role: .destructive) {
                            authSession.logout()
                        } label: {
                            Label("Sign Out", systemImage: "rectangle.portrait.and.arrow.right")
                        }
                    } label: {
                        Image(systemName: "person.circle.fill")
                            .font(.system(size: 28))
                            .foregroundStyle(.tint)
                    }
                }
            }
            .refreshable { viewModel.loadProfile() }
        }
    }
}

// MARK: - CategorySummaryHeader

private struct CategorySummaryHeader: View {
    let state: DashboardViewModel.UiState

    var body: some View {
        RoundedCard(color: Color(.secondarySystemBackground)) {
            HStack {
                CategoryChip(
                    title:  "Energy",
                    status: "\(String(format: "%.1f", state.solarOutputKw)) kW | \(state.batterySocPercent)%",
                    color:  Color(red: 0.18, green: 0.49, blue: 0.20)
                )

                Divider().frame(height: 28)

                CategoryChip(
                    title:  "Network",
                    status: state.isOfflineMode
                                ? "Offline (\(state.pendingSyncCount))"
                                : "Online",
                    color:  state.isOfflineMode ? .red : Color(red: 0.18, green: 0.49, blue: 0.20)
                )

                Divider().frame(height: 28)

                CategoryChip(
                    title:  "Security",
                    status: "Edge Locked",
                    color:  .accentColor
                )
            }
            .frame(maxWidth: .infinity)
        }
    }
}

private struct CategoryChip: View {
    let title:  String
    let status: String
    let color:  Color

    var body: some View {
        VStack(spacing: 2) {
            Text(title)
                .font(.caption2)
                .foregroundStyle(.secondary)

            HStack(spacing: 4) {
                Circle()
                    .fill(color)
                    .frame(width: 6, height: 6)
                Text(status)
                    .font(.caption)
                    .fontWeight(.bold)
                    .lineLimit(1)
            }
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - AiAgentCard

private struct AiAgentCard: View {
    let state:           DashboardViewModel.UiState
    let onGenerateClick: () -> Void

    var body: some View {
        RoundedCard(color: Color.accentColor.opacity(0.1)) {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Image(systemName: "brain.head.profile")
                        .foregroundStyle(.tint)
                    Text("Gemini Workflow Agent")
                        .font(.subheadline.bold())

                    Spacer()

                    Button(action: onGenerateClick) {
                        if state.isGeneratingDispatch {
                            ProgressView()
                                .scaleEffect(0.8)
                                .frame(width: 60, height: 28)
                        } else {
                            Text("Generate Plan")
                                .font(.caption.bold())
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(Color.accentColor)
                                .foregroundStyle(.white)
                                .clipShape(Capsule())
                        }
                    }
                    .disabled(state.isGeneratingDispatch)
                }

                if let rec = state.geminiRecommendation {
                    Text(rec)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }
}

// MARK: - MetricCard

private struct MetricCard: View {
    let title:     String
    let value:     String
    let subtitle:  String
    let icon:      String
    let iconColor: Color

    var body: some View {
        RoundedCard(color: Color(.systemBackground)) {
            VStack(alignment: .leading, spacing: 0) {
                HStack {
                    Text(title)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Spacer()
                    Image(systemName: icon)
                        .foregroundStyle(iconColor)
                        .font(.system(size: 18))
                }

                Spacer().frame(height: 12)

                Text(value)
                    .font(.title2.bold())

                Spacer().frame(height: 4)

                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

// MARK: - Shared card shell

private struct RoundedCard<Content: View>: View {
    let color:   Color
    let content: () -> Content

    init(color: Color = Color(.systemBackground), @ViewBuilder content: @escaping () -> Content) {
        self.color   = color
        self.content = content
    }

    var body: some View {
        content()
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(color)
            .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// MARK: - Preview

//#Preview {
//    let vm = DashboardViewModel.preview()
//    return DashboardView(viewModel: vm)
//        .environmentObject(AuthSession())
//}
