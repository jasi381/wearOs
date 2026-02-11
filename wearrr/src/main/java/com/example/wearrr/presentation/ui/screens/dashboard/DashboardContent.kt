package com.example.wearrr.presentation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wearrr.R
import com.example.wearrr.presentation.ui.screens.dashboard.components.ConnectionStatusChip
import com.example.wearrr.presentation.ui.screens.dashboard.components.UserProfileCard


/**
 * Dashboard Content Container
 *
 * Main scrollable container for all dashboard elements.
 * Organized in a logical top-to-bottom flow.
 */

@Composable
fun Lottie(
    lottieResId: Int,
    onAnimationEnd: () -> Unit,
    modifier: Modifier,
    iterations: Int = 1
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true,
        speed = 1f,
        restartOnPlay = false
    )

    // Only trigger once when fully complete
    val hasTriggered = remember { mutableStateOf(false) }

    if (progress >= 0.99f && !hasTriggered.value) {
        hasTriggered.value = true
        onAnimationEnd()
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
        contentScale = ContentScale.FillBounds
    )
}


// NavGraph setup
@Composable
fun WatchNavGraph(
    navController: NavHostController = rememberSwipeDismissableNavController(),
    userName: String,
    groupName: String,
    dashboardState: DashboardState,
    listState: ScalingLazyListState,
    onFallTest: (String) -> Unit,
    onSimulationToggle: () -> Unit,
    userImage: String
) {
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable(
            route = "splash",
        ) {
            ActiveMonitoringScreen(
                onAnimationEnd = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "dashboard",
        ) {
            DashboardScreen(
                userName = userName,
                groupName = groupName,
                dashboardState = dashboardState,
                listState = listState,
                onFallTest = onFallTest,
                onSimulationToggle = onSimulationToggle,
                userImage = userImage
            )
        }
    }
}


// Splash Screen
@Composable
fun ActiveMonitoringScreen(onAnimationEnd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff000719)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Lottie(
                lottieResId = R.raw.active,
                modifier = Modifier.size(60.dp),
                onAnimationEnd = onAnimationEnd,
                iterations = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Actively Monitoring",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Fall Guard Active",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        }
    }
}


// Dashboard Screen
@Composable
fun DashboardScreen(
    userName: String,
    groupName: String,
    dashboardState: DashboardState,
    listState: ScalingLazyListState,
    onFallTest: (String) -> Unit,
    onSimulationToggle: () -> Unit,
    userImage: String
) {
    ScalingLazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff000719))
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(top = 4.dp),
        autoCentering = null
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            UserProfileCard(
                userName = userName,
                groupName = groupName,
                userImage = userImage
            )
        }

        item {
            ConnectionStatusChip(isConnected = dashboardState.isConnectedToPhone)
        }

//        item {
//            MonitoringStatusCard(
//                isMonitoring = dashboardState.isMonitoring,
//                simulationMode = dashboardState.simulationMode
//            )
//        }
//
//        item {
//            FallTestControls(onFallTest = onFallTest)
//        }
//
//        item {
//            SimulationToggle(
//                simulationMode = dashboardState.simulationMode,
//                onToggle = onSimulationToggle
//            )
//        }
//
//        if (dashboardState.lastFallTime > 0) {
//            item {
//                LastFallIndicator(lastFallTime = dashboardState.lastFallTime)
//            }
//        }
//
//        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}