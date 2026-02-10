package com.example.wearrr.presentation

import WearCapabilityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearrr.DummyMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class WearUiState {
    object Checking : WearUiState()
    object PhoneNotInstalled : WearUiState()
    object Connected : WearUiState()
}

class WearViewModel(
    context: Context
) : ViewModel() {

    private val manager = WearCapabilityManager(context)

    val uiState: StateFlow<WearUiState> =
        manager.mobileInstalled
            .map { installed ->
                when (installed) {
                    null -> WearUiState.Checking
                    true -> WearUiState.Connected
                    false -> WearUiState.PhoneNotInstalled
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                WearUiState.Checking
            )

    val receivedMessage: StateFlow<DummyMessage?> = manager.receivedMessage

    fun start() = manager.start()
    fun stop() = manager.stop()
}
