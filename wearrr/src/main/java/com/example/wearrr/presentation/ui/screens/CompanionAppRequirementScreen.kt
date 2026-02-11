package com.example.wearrr.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.wearrr.R
import com.example.wearrr.presentation.theme.FallGuardColors


/**
 * Created by Jasmeet Singh on 04/12/25.
 */


/**
 * Mobile App Required Screen
 *
 * Displayed when the companion mobile app is not detected.
 */

@Composable
fun MobileAppRequiredScreen(onCheckAgain: () -> Unit = {}) {
    val listState = rememberScalingLazyListState()

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

            item {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_warn),
                    contentDescription = "Warning",
                    tint = FallGuardColors.warning,
                    modifier = Modifier.size(48.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "Mobile App Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = FallGuardColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "Please install the Fall Detection app on your phone to continue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FallGuardColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = onCheckAgain,
                    modifier = Modifier.height(48.dp),
                    label = {
                        Text(
                            text = "Check Again",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}