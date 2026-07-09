package com.identityx.authentication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.identityx.android.core.network.model.UserProfile

@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreenContent(
        uiState = uiState,
        onLogout = onLogout,
        onRetry = { viewModel.loadProfile() }
    )
}

@Composable
private fun DashboardScreenContent(
    uiState: DashboardViewModel.DashboardUiState,
    onLogout: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )

        // Profile card
        ProfileCard(
            uiState = uiState,
            onRetry = onRetry
        )

        // Always-visible refresh button — useful for manually re-fetching
        // and for testing token refresh / forced logout behaviour
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is DashboardViewModel.DashboardUiState.Loading
        ) {
            Text("Refresh Profile")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor   = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun ProfileCard(
    uiState: DashboardViewModel.DashboardUiState,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is DashboardViewModel.DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }

                is DashboardViewModel.DashboardUiState.Success -> {
                    ProfileContent(profile = uiState.profile)
                }

                is DashboardViewModel.DashboardUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Failed to load profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(profile: UserProfile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Your Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        ProfileRow(label = "Name",   value = profile.displayName)
        ProfileRow(label = "Email",  value = profile.email)
        ProfileRow(label = "Plan",   value = profile.plan)
        ProfileRow(label = "ID",     value = profile.userId)
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardLoadingPreview() {
    DashboardScreenContent(
        uiState = DashboardViewModel.DashboardUiState.Loading,
        onLogout = {},
        onRetry = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun DashboardSuccessPreview() {
    DashboardScreenContent(
        uiState = DashboardViewModel.DashboardUiState.Success(
            UserProfile(
                userId      = "user_001",
                email       = "test@identityx.com",
                displayName = "Identity X User",
                plan        = "Premium"
            )
        ),
        onLogout = {},
        onRetry = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun DashboardErrorPreview() {
    DashboardScreenContent(
        uiState = DashboardViewModel.DashboardUiState.Error("Token expired"),
        onLogout = {},
        onRetry = {}
    )
}
