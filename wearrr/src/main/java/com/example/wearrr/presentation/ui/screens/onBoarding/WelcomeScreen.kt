package com.example.wearrr.presentation.ui.screens.onBoarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
 * Welcome Step - Simple and centered
 */
@Preview(
    device = "id:wearos_large_round",
    showSystemUi = true,
    backgroundColor = 0xFF0A0E27
)
@Composable
fun WelcomeStep(navController: NavHostController? = null) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff000719))
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_step_1_3),
            contentDescription = "Fall Guard",
            modifier = Modifier
                .padding(top = 8.dp)
                .size(64.dp)
                .scale(scale.value)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
            text = "Fall Guard",
            style = MaterialTheme.typography.titleLarge,
            color = FallGuardColors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        Text(
            text = "Your Safety Companion",
            style = MaterialTheme.typography.bodyMedium,
            color = FallGuardColors.textSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Next Button - ALWAYS VISIBLE
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color(0xff13AE8C), MaterialTheme.shapes.large)
                .padding(vertical = 6.dp, horizontal = 16.dp)
                .clickableNoRipple(
                    onClick = { navController?.navigate(OnboardingRoute.Features.route) },
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
}