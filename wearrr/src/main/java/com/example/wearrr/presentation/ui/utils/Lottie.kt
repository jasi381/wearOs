package com.example.wearrr.presentation.ui.utils

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun Lottie(
    @RawRes lottieResId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    onAnimationEnd: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations
    )

    if (progress == 1f) {
        onAnimationEnd()
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}