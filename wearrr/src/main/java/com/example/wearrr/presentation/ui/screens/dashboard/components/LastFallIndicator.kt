package com.example.wearrr.presentation.ui.screens.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Last Fall Indicator
 *
 * Small text displaying time since last detected fall.
 * Updates automatically as time passes.
 */
@Composable
fun LastFallIndicator(lastFallTime: Long) {
    val timeSince = (System.currentTimeMillis() - lastFallTime) / 1000
    Text(
        text = "Last fall: ${timeSince}s ago",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        fontSize = 11.sp
    )
}
