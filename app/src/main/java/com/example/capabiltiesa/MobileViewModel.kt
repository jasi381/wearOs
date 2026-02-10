package com.example.capabiltiesa

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class MobileUiState {
    object Checking : MobileUiState()
    object WearNotInstalled : MobileUiState()
    object Connected : MobileUiState()
}

class MobileViewModel(
    context: Context
) : ViewModel() {

    private val manager = MobileCapabilityManager(context)

    val uiState: StateFlow<MobileUiState> =
        manager.wearInstalled
            .map { installed ->
                when (installed) {
                    null -> MobileUiState.Checking
                    true -> MobileUiState.Connected
                    false -> MobileUiState.WearNotInstalled
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                MobileUiState.Checking
            )

    fun start() = manager.start()
    fun stop() = manager.stop()
}
