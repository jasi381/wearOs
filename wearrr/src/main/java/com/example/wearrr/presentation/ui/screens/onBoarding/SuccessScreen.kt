package com.example.wearrr.presentation.ui.screens.onBoarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.wearrr.R
import com.example.wearrr.presentation.theme.FallGuardColors
import com.example.wearrr.presentation.ui.utils.Lottie

/**
 * Created by Jasmeet Singh on 03/12/25.
 */


/**
 * Success Step - Auto-completes
 */
@Preview
@Composable
fun SuccessStep(onComplete: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Lottie(
            modifier = Modifier.size(90.dp),
            iterations = 1,
            onAnimationEnd = onComplete,
            lottieResId = R.raw.done
        )



        Text(
            text = "All Set!",
            style = MaterialTheme.typography.titleLarge,
            color = FallGuardColors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fall Guard is protecting you",
            style = MaterialTheme.typography.bodyMedium,
            color = FallGuardColors.textSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}