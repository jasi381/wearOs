package com.example.wearrr.presentation

import WearCapabilityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearrr.DummyMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed class WearUiState {
    object Checking : WearUiState()
    object PhoneNotInstalled : WearUiState()
    object Disconnected : WearUiState()
    object Connected : WearUiState()
}

class WearViewModel(
    context: Context
) : ViewModel() {

    private val manager = WearCapabilityManager(context)

    val uiState: StateFlow<WearUiState> =
        combine(manager.peerInstalled, manager.peerReachable) { installed, reachable ->
            when {
                installed == null || reachable == null -> WearUiState.Checking
                reachable -> WearUiState.Connected
                installed -> WearUiState.Disconnected
                else -> WearUiState.PhoneNotInstalled
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            WearUiState.Checking
        )

    val receivedMessage: StateFlow<DummyMessage?> = manager.receivedMessage
    val isLoggedIn: StateFlow<Boolean> = manager.isLoggedIn

    fun start() = manager.start()
    fun stop() = manager.stop()
}
