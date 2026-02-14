package com.example.capabiltiesa

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed class MobileUiState {
    object Checking : MobileUiState()
    object WearNotInstalled : MobileUiState()
    object Disconnected : MobileUiState()
    object Connected : MobileUiState()
}

class MobileViewModel(
    private val context: Context
) : ViewModel() {

    private val manager = MobileCapabilityManager(context)

    val uiState: StateFlow<MobileUiState> =
        combine(manager.peerInstalled, manager.peerReachable) { installed, reachable ->
            when {
                installed == null || reachable == null -> MobileUiState.Checking
                reachable -> MobileUiState.Connected
                installed -> MobileUiState.Disconnected
                else -> MobileUiState.WearNotInstalled
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MobileUiState.Checking
        )

    private val _onScreen2 = MutableStateFlow(false)
    val onScreen2: StateFlow<Boolean> = _onScreen2

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun start() = manager.start()
    fun stop() = manager.stop()

    fun login(email: String, password: String): Boolean {
        // Dummy login validation
        if (email == "sjasmeet438@gmail.com" && password == "123456") {
            _isLoggedIn.value = true
            // Send login status to watch
            manager.sendLoginStatus(
                LoginMessage(
                    isLoggedIn = true,
                    email = email
                )
            )
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        manager.sendLoginStatus(
            LoginMessage(
                isLoggedIn = false,
                email = null
            )
        )
    }

    fun navigateToScreen2() {
        _onScreen2.value = true
    }

    fun navigateBack() {
        _onScreen2.value = false
    }

    fun sendProfile(name: String, imageUrl: String?) {
        manager.sendMessage(
            DummyMessage(
                name = name,
                photoUrl = imageUrl?.takeIf { it.isNotBlank() },
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
