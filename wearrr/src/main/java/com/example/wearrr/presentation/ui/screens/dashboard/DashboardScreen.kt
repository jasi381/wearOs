package com.example.wearrr.presentation.ui.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.example.wearrr.fall.FallDetectionService
import com.example.wearrr.presentation.theme.WearOsAppTheme
import com.example.wearrr.presentation.ui.screens.MobileAppRequiredScreen
import com.kinected.kinectedcarereceiver.presentation.ui.dashboard.utils.checkMobileAppCapability
import com.kinected.kinectedcarereceiver.presentation.ui.dashboard.utils.checkPhoneConnectivity
import kotlinx.coroutines.delay

/**
 * Fall Guard Main Dashboard
 *
 * The primary interface for monitoring fall detection status and controls.
 * Refactored to follow Material Design principles and modern Compose architecture.
 *
 * Architecture:
 * - State Holder pattern for state management
 * - Modular composables for each UI section
 * - Separation of concerns (UI, state, business logic)
 * - Clear unidirectional data flow
 *
 * Material Design Principles:
 * - Card-based layout for clear visual separation
 * - Large touch targets (minimum 48dp) for accessibility
 * - Color-coded status indicators for quick recognition
 * - Proper visual hierarchy with size, color, and spacing
 * - Animated state transitions for premium feel
 * - OLED-optimized dark theme with radial gradient
 *
 * Accessibility:
 * - All buttons exceed 48dp minimum touch target
 * - High contrast colors (WCAG AA compliant)
 * - Descriptive content descriptions for screen readers
 * - No color-only indicators (icon + color + text)
 *
 * Created by Jasmeet Singh on 04/12/25.
 */
@Preview(
    device = "id:wearos_large_round",
    showSystemUi = true,
    backgroundColor = 0xFF0A0E27
)
@Composable
fun FallGuardDashboard() {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()
    val stateHolder = rememberDashboardState()
    val dashboardState = stateHolder.state

    // Retrieve user data for display
    val userName = "Noobie"
    val userImage = "https://picsum.photos/200/300"
    val groupName = "Test group"

    // Check mobile app installation - manual trigger
    LaunchedEffect(dashboardState.checkAppTrigger) {
        if (dashboardState.checkAppTrigger > 0) {
            checkMobileAppCapability(context) { isInstalled ->
                stateHolder.updateAppInstallationStatus(isInstalled)
            }
        }
    }

    // Periodic connectivity and app installation checks
    LaunchedEffect(Unit) {
        while (true) {
            checkMobileAppCapability(context) { isInstalled ->
                stateHolder.updateAppInstallationStatus(isInstalled)
            }
            checkPhoneConnectivity(context) { isConnected, nodeName ->
                stateHolder.updateConnectionStatus(isConnected, nodeName)
            }
            delay(3000) // Check every 3 seconds for faster detection
        }
    }

    WearOsAppTheme {
        // Show instruction screen if mobile app not installed
        if (!dashboardState.isMobileAppInstalled) {
            MobileAppRequiredScreen(
                onCheckAgain = { stateHolder.triggerAppCheck() }
            )
        } else {
            WatchNavGraph(
                userName = userName,
                groupName = groupName,
                userImage = userImage,
                dashboardState = dashboardState,
                listState = listState,
                onFallTest = { fallType ->
//                    FallDetectionService.simulateFall(fallType)
                    stateHolder.updateLastFallTime(System.currentTimeMillis())
                },
                onSimulationToggle = {
                    stateHolder.toggleSimulationMode()
//                    FallDetectionService.toggleSimulationMode(dashboardState.simulationMode)
                }
            )
        }
    }
}
