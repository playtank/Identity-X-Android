package com.identityx.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreen_rendersCoreComponents() {
        val uiState = DashboardUiState(
            userName          = "Alvin Lei",
            userEmail         = "alvin.lei@example.com",
            isOfflineMode     = true,
            pendingSyncCount  = 5,
            solarOutputKw     = 7.2,
            batterySocPercent = 90
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState                 = uiState,
                onSignOutClick          = {},
                onGenerateAgentDispatch = {},
                onCardClick             = {}
            )
        }

        // Top bar title and offline status
        composeTestRule.onNodeWithText("Identity-X Energy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Offline Mode (5 pending)").assertIsDisplayed()

        // Category summary header chips
        composeTestRule.onNodeWithText("Energy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network").assertIsDisplayed()
        composeTestRule.onNodeWithText("Security").assertIsDisplayed()

        // Metric cards
        composeTestRule.onNodeWithText("Solar Output").assertIsDisplayed()
        composeTestRule.onNodeWithText("7.2 kW").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery Level").assertIsDisplayed()
        composeTestRule.onNodeWithText("90%").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_logoutMenuInteraction() {
        var signOutClicked = false

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState                 = DashboardUiState(
                    userName  = "Alvin Lei",
                    userEmail = "alvin.lei@example.com"
                ),
                onSignOutClick          = { signOutClicked = true },
                onGenerateAgentDispatch = {},
                onCardClick             = {}
            )
        }

        // Open the user profile dropdown
        composeTestRule.onNodeWithContentDescription("User Profile").performClick()

        // Verify user info and Sign Out option are visible
        composeTestRule.onNodeWithText("Alvin Lei").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()

        // Click Sign Out
        composeTestRule.onNodeWithText("Sign Out").performClick()

        assert(signOutClicked) { "Sign Out callback was not triggered!" }
    }

    @Test
    fun dashboardScreen_metricCardClicks() {
        var clickedCardId = ""

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState                 = DashboardUiState(),
                onSignOutClick          = {},
                onGenerateAgentDispatch = {},
                onCardClick             = { cardId -> clickedCardId = cardId }
            )
        }

        composeTestRule.onNodeWithText("Solar Output").performClick()
        assert(clickedCardId == "solar") {
            "Expected 'solar' but got '$clickedCardId'"
        }

        composeTestRule.onNodeWithText("Battery Level").performClick()
        assert(clickedCardId == "battery") {
            "Expected 'battery' but got '$clickedCardId'"
        }
    }
}
