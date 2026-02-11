package com.example.wearrr.presentation.ui.screens.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.wearrr.R
import com.example.wearrr.presentation.theme.FallGuardColors


/**
 * Monitoring Status Card Component
 *
 * Large animated card showing current monitoring state.
 * Features:
 * - Animated icon with pulsing effect when active
 * - Color-coded status (teal=monitoring, orange=simulation, red=stopped)
 * - Clear status text and subtitle
 * - Icon + color + text for accessibility (no color-only indicators)
 *
 * Animations:
 * - Pulsing scale animation (1.0x to 1.1x) when monitoring active
 * - Smooth color transitions on state changes
 */
@Composable
fun MonitoringStatusCard(
    isMonitoring: Boolean,
    simulationMode: Boolean
) {
    // Determine status attributes
    val statusColor = FallGuardColors.getStatusColor(isMonitoring, simulationMode)
    val statusText = when {
        simulationMode -> "Simulation Mode"
        isMonitoring -> "Actively Monitoring"
        else -> "Monitoring Stopped"
    }
    val statusIcon = ImageVector.vectorResource(R.drawable.ic_warn)
    val subtitle = if (isMonitoring || simulationMode) "Fall Guard Active" else "Tap to enable"

    // Pulsing animation for active states
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(isMonitoring, simulationMode) {
        if (isMonitoring || simulationMode) {
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            while (true) {
                pulseScale.animateTo(
                    targetValue = 1.1f,
                    animationSpec = tween(1200)
                )
                pulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1200)
                )
            }
        } else {
            pulseScale.snapTo(1f)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Animated Status Icon with glow effect
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp)
        ) {
            // Outer glow layer (20% opacity, slightly larger)
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale.value)
            )
            // Main icon
            Icon(
                imageVector = statusIcon,
                contentDescription = "Status: $statusText",
                tint = statusColor,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Text
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 11.sp
        )
    }
}
