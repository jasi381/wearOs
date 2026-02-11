package com.example.wearrr.presentation.ui.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Dashboard State Data Class
 *
 * Encapsulates all state for the Fall Guard dashboard following
 * the State Holder pattern for better testability and separation of concerns.
 *
 * @property isMonitoring Whether fall detection monitoring is active
 * @property lastFallTime Timestamp of the last detected fall
 * @property simulationMode Whether simulation mode is enabled
 * @property isConnectedToPhone Phone connectivity status
 * @property connectedNodeName Name of the connected device
 * @property isMobileAppInstalled Whether the mobile app is installed
 * @property checkAppTrigger Counter to trigger manual app capability checks
 */
@Stable
data class DashboardState(
    val isMonitoring: Boolean = true,
    val lastFallTime: Long = 0L,
    val simulationMode: Boolean = false,
    val isConnectedToPhone: Boolean = false,
    val connectedNodeName: String = "",
    val isMobileAppInstalled: Boolean = true,
    val checkAppTrigger: Int = 0
)

/**
 * Dashboard State Holder
 *
 * Manages state updates for the dashboard following best practices
 * for Jetpack Compose state management.
 */
@Composable
fun rememberDashboardState(): DashboardStateHolder {
    return remember { DashboardStateHolder() }
}

@Stable
class DashboardStateHolder {
    var state by mutableStateOf(DashboardState())
        private set

    fun updateMonitoring(isMonitoring: Boolean) {
        state = state.copy(isMonitoring = isMonitoring)
    }

    fun updateLastFallTime(time: Long) {
        state = state.copy(lastFallTime = time)
    }

    fun toggleSimulationMode() {
        state = state.copy(simulationMode = !state.simulationMode)
    }

    fun updateConnectionStatus(isConnected: Boolean, nodeName: String) {
        state = state.copy(
            isConnectedToPhone = isConnected,
            connectedNodeName = nodeName
        )
    }

    fun updateAppInstallationStatus(isInstalled: Boolean) {
        state = state.copy(isMobileAppInstalled = isInstalled)
    }

    fun triggerAppCheck() {
        state = state.copy(checkAppTrigger = state.checkAppTrigger + 1)
    }
}
