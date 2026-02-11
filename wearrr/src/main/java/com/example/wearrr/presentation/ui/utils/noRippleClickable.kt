package com.example.wearrr.presentation.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role

fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    hapticFeedback: Boolean = false,
    onClick: () -> Unit
) = composed {
    val haptics = LocalHapticFeedback.current

    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        if (hapticFeedback) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        onClick()
    }
}