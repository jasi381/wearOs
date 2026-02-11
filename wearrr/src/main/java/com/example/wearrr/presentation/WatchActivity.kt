package com.example.wearrr.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.wearrr.fall.FallDetectionService
import com.example.wearrr.presentation.theme.CapabiltiesaTheme


class WatchActivity : ComponentActivity() {

    private lateinit var viewModel: WearViewModel

    private val bodySensorsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("WatchActivity", "BODY_SENSORS permission result: $granted")
        if (granted) {
            startFallDetection()
        } else {
            Log.w("WatchActivity", "BODY_SENSORS denied — starting service anyway (accel may still work)")
            startFallDetection()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        viewModel = ViewModelProvider(
            this,
            WearViewModelFactory(applicationContext)
        )[WearViewModel::class.java]

        setContent {
            CapabiltiesaTheme {
                WearHomeScreen(viewModel)
            }
        }

        // Accelerometer does NOT require BODY_SENSORS (that's for heart rate/SpO2).
        // Start fall detection service directly. Request BODY_SENSORS in background
        // for future biometric features.
        startFallDetection()
        requestBodySensorsPermission()
    }

    private fun requestBodySensorsPermission() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) ==
            PackageManager.PERMISSION_GRANTED
        Log.d("WatchActivity", "BODY_SENSORS already granted: $granted")
        if (!granted) {
            bodySensorsPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
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

