package com.example.wearrr.presentation.ui.screens.onBoarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.wearrr.R
import com.example.wearrr.presentation.ui.utils.clickableNoRipple
import com.example.wearrr.presentation.theme.FallGuardColors

/**
 * Created by Jasmeet Singh on 03/12/25.
 */


/**
 * Features Step - Simple list, no scrolling
 */
@Preview(
    device = "id:wearos_large_round",
    showSystemUi = true,
    backgroundColor = 0xFF0A0E27
)
@Composable
fun FeaturesStep(
    navController: NavHostController? = null
) {
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff000719))
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        // Add these to remove the extra top padding
        contentPadding = PaddingValues(top = 22.dp),
        autoCentering = null  // Disable auto-centering
    ) {
        item {
            Text(
                text = "Key Features",
                style = MaterialTheme.typography.titleMedium,
                color = FallGuardColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            FeatureItem(
                icon = ImageVector.vectorResource(R.drawable.layer_1),
                title = "Auto Monitoring",
                color = Color(0xff00D498),
                backGroundColor = Color(0xff002C2F)
            )
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            FeatureItem(
                icon = ImageVector.vectorResource(R.drawable.ic_notification),
                title = "Instant Alerts",
                color = FallGuardColors.accent2
            )
        }


        item { Spacer(modifier = Modifier.height(14.dp)) }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color(0xff13AE8C), MaterialTheme.shapes.large)
                    .padding(vertical = 6.dp, horizontal = 30.dp)
                    .clickableNoRipple(
                        onClick = { navController?.navigate(OnboardingRoute.Permission.route) },
                        hapticFeedback = true,
                        role = Role.Button
                    )
                    .clip(MaterialTheme.shapes.large)
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_forward),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color(0xff333847), MaterialTheme.shapes.large)
                    .padding(vertical = 6.dp, horizontal = 30.dp)
                    .clickableNoRipple(
                        onClick = { navController?.popBackStack() },
                        hapticFeedback = true,
                        role = Role.Button
                    )
                    .clip(MaterialTheme.shapes.large)
            ) {

                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }

    }
}

/**
 * Simple Feature Item
 */
@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    color: Color,
    backGroundColor: Color = Color.Transparent
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        // Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = if (backGroundColor == Color.Transparent) color.copy(alpha = 0.2f) else backGroundColor,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

// Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = FallGuardColors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}