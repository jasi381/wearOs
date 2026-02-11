package com.example.wearrr.presentation

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.wearrr.fall.FallDetectionService
import com.example.wearrr.presentation.theme.CapabiltiesaTheme
import com.example.wearrr.presentation.ui.screens.IncompatibleDeviceScreen
import com.example.wearrr.presentation.ui.screens.dashboard.FallGuardDashboard
import com.example.wearrr.presentation.ui.screens.onBoarding.OnboardingScreen


class WatchActivity : ComponentActivity() {

    private lateinit var viewModel: WearViewModel

    private var hasRequiredSensors = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        val sensorManager = getSystemService(SensorManager::class.java)
        hasRequiredSensors = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

        viewModel = ViewModelProvider(
            this,
            WearViewModelFactory(applicationContext)
        )[WearViewModel::class.java]

        setContent {
            CapabiltiesaTheme {

                var showOnboarding by remember { mutableStateOf(!isPermissionGranted()) }

                when {
                    !hasRequiredSensors -> {
                        IncompatibleDeviceScreen()
                    }

                    showOnboarding -> {
                        OnboardingScreen(
                            onPermissionGranted = {
                                showOnboarding = false
                                startFallDetection()
                            }
                        )
                    }

                    else -> {
                        LaunchedEffect(Unit) {
                           startFallDetection()
                        }
                        WearHomeScreen(viewModel)
                    }
                }
            }
        }
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startFallDetection() {
        Log.d("WatchActivity", "Starting FallDetectionService")
        val intent = Intent(this, FallDetectionService::class.java)
        startForegroundService(intent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.start()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stop()
    }
}
class WearViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WearViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WearViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

