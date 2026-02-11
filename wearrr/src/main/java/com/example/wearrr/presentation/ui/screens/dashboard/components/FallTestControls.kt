package com.example.wearrr.presentation.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.wearrr.R
import com.example.wearrr.presentation.theme.FallGuardColors

/**
 * Fall Test Controls
 *
 * Row of buttons to simulate different types of falls for testing.
 * Organized as a horizontal row with appropriate spacing.
 *
 * Material Design:
 * - 56dp x 56dp buttons (exceeds 48dp minimum touch target)
 * - Color-coded by severity (red for high impact, orange for slow fall)
 * - Icon + text for clarity
 */
@Composable
fun FallTestControls(
    onFallTest: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TestFallButton(
            label = "FALL",
            onClick = { onFallTest("high_impact") },
            containerColor = MaterialTheme.colorScheme.error
        )

        TestFallButton(
            label = "SLOW",
            onClick = { onFallTest("gradual_fall") },
            containerColor = FallGuardColors.warning
        )
    }
}

/**
 * Test Fall Button Component
 *
 * Compact button for simulating fall events during testing.
 *
 * Material Design:
 * - 56dp x 56dp size (exceeds 48dp minimum touch target)
 * - Vertical layout: icon above text
 * - Bold uppercase text for clarity
 * - Color-coded by severity
 *
 * Accessibility:
 * - Large touch target
 * - Icon + text for clear meaning
 * - High contrast white text on colored background
 */
@Composable
private fun TestFallButton(
    label: String,
    onClick: () -> Unit,
    containerColor: Color
) {
    CompactButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_warn),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyExtraSmall,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
