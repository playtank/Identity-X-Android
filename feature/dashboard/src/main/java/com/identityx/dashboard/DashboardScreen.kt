package com.identityx.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Public entry point ────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    onSignOutClick: () -> Unit = {},
    onCardClick: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreenContent(
        uiState                 = uiState,
        onSignOutClick          = onSignOutClick,
        onGenerateAgentDispatch = viewModel::onGenerateAgentDispatch,
        onCardClick             = onCardClick
    )
}

// ── Internal content ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenContent(
    uiState: DashboardUiState,
    onSignOutClick: () -> Unit,
    onGenerateAgentDispatch: () -> Unit,
    onCardClick: (String) -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Identity-X Energy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isOfflineMode)
                                "Offline Mode (${uiState.pendingSyncCount} pending)"
                            else
                                "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isOfflineMode)
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF2E7D32)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showUserMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = uiState.userName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = uiState.userEmail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                            Divider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sign Out", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    onSignOutClick()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item(span = { GridItemSpan(2) }) {
                CategorySummaryHeader(uiState = uiState)
            }

            item(span = { GridItemSpan(2) }) {
                AiAgentCard(
                    uiState = uiState,
                    onGenerateClick = onGenerateAgentDispatch
                )
            }

            item {
                DashboardMetricCard(
                    title    = "Solar Output",
                    value    = "${uiState.solarOutputKw} kW",
                    subtitle = "Generation Active",
                    icon     = Icons.Default.SolarPower,
                    iconTint = Color(0xFFF57C00),
                    onClick  = { onCardClick("solar") }
                )
            }

            item {
                DashboardMetricCard(
                    title    = "Battery Level",
                    value    = "${uiState.batterySocPercent}%",
                    subtitle = "15.1 kWh Remaining",
                    icon     = Icons.Default.Bolt,
                    iconTint = Color(0xFF388E3C),
                    onClick  = { onCardClick("battery") }
                )
            }

            item {
                DashboardMetricCard(
                    title    = "Grid Rate",
                    value    = uiState.peakTariffRate,
                    subtitle = "Off-Peak Tariff",
                    icon     = Icons.Default.Refresh,
                    iconTint = Color(0xFF1976D2),
                    onClick  = { onCardClick("grid") }
                )
            }

            item {
                DashboardMetricCard(
                    title    = "Sync Queue",
                    value    = "${uiState.pendingSyncCount} Items",
                    subtitle = if (uiState.isOfflineMode) "Offline Stored" else "Synced",
                    icon     = Icons.Default.SignalWifiOff,
                    iconTint = if (uiState.isOfflineMode)
                        MaterialTheme.colorScheme.error
                    else
                        Color(0xFF388E3C),
                    onClick  = { onCardClick("sync") }
                )
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun CategorySummaryHeader(uiState: DashboardUiState) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryChip(
                title  = "Energy",
                status = "${uiState.solarOutputKw} kW | ${uiState.batterySocPercent}%",
                color  = Color(0xFF2E7D32)
            )
            Divider(modifier = Modifier.height(28.dp).width(1.dp))
            CategoryChip(
                title  = "Network",
                status = if (uiState.isOfflineMode)
                    "Offline (${uiState.pendingSyncCount})" else "Online",
                color  = if (uiState.isOfflineMode)
                    MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            )
            Divider(modifier = Modifier.height(28.dp).width(1.dp))
            CategoryChip(
                title  = "Security",
                status = "Edge Locked",
                color  = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryChip(title: String, status: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text     = status,
                style    = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AiAgentCard(
    uiState: DashboardUiState,
    onGenerateClick: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector     = Icons.Default.Psychology,
                        contentDescription = "Gemini Agent",
                        tint            = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "Gemini Workflow Agent",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick        = onGenerateClick,
                    enabled        = !uiState.isGeneratingDispatch,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    if (uiState.isGeneratingDispatch) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Generate Plan", fontSize = 12.sp)
                    }
                }
            }
            uiState.geminiRecommendation?.let { recommendation ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector        = icon,
                    contentDescription = title,
                    tint               = iconTint,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
