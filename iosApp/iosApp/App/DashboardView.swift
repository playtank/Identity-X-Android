//
//  DashboardView.swift
//  iosApp
//
//  Placeholder dashboard shown after successful login.
//  Replace with real dashboard content as the app grows.
//

import SwiftUI

struct DashboardView: View {

    @EnvironmentObject private var authSession: AuthSession

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Image(systemName: "checkmark.seal.fill")
                    .font(.system(size: 72))
                    .foregroundStyle(.green)

                Text("Welcome to Identity X")
                    .font(.title2.bold())

                Text("You are signed in.")
                    .foregroundStyle(.secondary)

                Spacer()

                Button(role: .destructive) {
                    authSession.logout()
                } label: {
                    Label("Sign Out", systemImage: "rectangle.portrait.and.arrow.right")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 40)
            }
            .padding(.top, 80)
            .navigationTitle("Dashboard")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

#Preview {
    DashboardView()
        .environmentObject(AuthSession())
}
