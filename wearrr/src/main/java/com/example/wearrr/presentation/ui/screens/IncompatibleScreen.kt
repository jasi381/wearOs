package com.example.wearrr.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.wearrr.R
import com.example.wearrr.presentation.theme.FallGuardColors
import com.example.wearrr.presentation.theme.WearOsAppTheme

/**
 * Created by Jasmeet Singh on 04/12/25.
 */


/**
 * Incompatible Device Screen
 *
 * Displayed when required sensors (accelerometer, gyroscope) are missing.
 *
 * Material Design Principles:
 * - Clear error communication with helpful context
 * - Semantic color usage (error red for warnings)
 * - Proper visual hierarchy with icon, title, and details
 * - High contrast text for readability
 * - Informative rather than alarming tone
 *
 * Accessibility:
 * - Content description on functional icons
 * - WCAG AA compliant color contrast
 * - Clear, readable text with appropriate sizing
 */
@Preview(
    device = "id:wearos_large_round",
    showSystemUi = true,
    backgroundColor = 0xFF0A0E27
)
@Composable
fun IncompatibleDeviceScreen() {
    val listState = rememberScalingLazyListState()

    WearOsAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = FallGuardColors.backgroundGradient,
                        radius = 300f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                state = listState
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Error icon with glow effect
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        // Outer glow layer
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_warn),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            modifier = Modifier.size(80.dp)
                        )
                        // Main icon
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_warn),
                            contentDescription = "Device Incompatible",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    Text(
                        text = "Device Not Compatible",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text(
                        text = "This watch is missing required sensors:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Sensor requirements with bullet points
                item {
                    SensorRequirement("Accelerometer")
                }
                item {
                    SensorRequirement("Gyroscope")
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    Text(
                        text = "Fall detection cannot function without these sensors",
                        style = MaterialTheme.typography.labelSmall,
                        color = FallGuardColors.textTertiary,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

/**
 * Sensor Requirement List Item
 *
 * Displays a single missing sensor requirement with a bullet point.
 */
@Composable
private fun SensorRequirement(sensorName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        // Red bullet point
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = sensorName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp
        )
    }
}
