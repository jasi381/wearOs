package com.example.wearrr.presentation.ui.screens.onBoarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

sealed class OnboardingRoute(val route: String) {
    data object Welcome : OnboardingRoute("welcome")
    data object Features : OnboardingRoute("features")
    data object Permission : OnboardingRoute("permission")
    data object Success : OnboardingRoute("success")
}

/**
 * Simple, Clean Onboarding Screen with Navigation
 * Uses Wear OS Navigation for proper screen transitions
 */
@Preview(
    device = "id:wearos_large_round",
    showSystemUi = true,
    backgroundColor = 0xFF0A0E27
)
@Composable
fun OnboardingScreen(
    onPermissionGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberSwipeDismissableNavController()

    // Check if permission is already granted on first load only
    val isPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (isPermissionGranted) {
            onPermissionGranted()
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.navigate(OnboardingRoute.Success.route) {
                popUpTo(OnboardingRoute.Welcome.route) { inclusive = true }
            }
        }
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = OnboardingRoute.Welcome.route
    ) {
        composable(OnboardingRoute.Welcome.route) {
            WelcomeStep(navController = navController)
        }

        composable(OnboardingRoute.Features.route) {
            FeaturesStep(navController = navController)
        }

        composable(OnboardingRoute.Permission.route) {
            PermissionStep(
                navController = navController,
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            )
        }

        composable(OnboardingRoute.Success.route) {
            SuccessStep(onComplete = onPermissionGranted)
        }
    }
}
