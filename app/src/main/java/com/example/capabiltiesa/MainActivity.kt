package com.example.capabiltiesa

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.capabiltiesa.ui.theme.CapabiltiesaTheme
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_FALL_DETECTED = "fall_detected"
        const val EXTRA_FALL_TIMESTAMP = "fall_timestamp"
    }

    private lateinit var viewModel: MobileViewModel
    private var showFallDialog = mutableStateOf(false)
    private var fallTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this,
            MobileViewModelFactory(applicationContext)
        )[MobileViewModel::class.java]

        checkFullScreenIntentPermission()
        handleFallIntent(intent)

        setContent {
            CapabiltiesaTheme {
                MobileHomeScreen(viewModel)

                if (showFallDialog.value) {
                    FallDetectedDialog(
                        onImOk = {
                            showFallDialog.value = false
                            sendActionToHandler(FallAlertHandlerService.ACTION_OK)
                        },
                        onNeedHelp = {
                            showFallDialog.value = false
                            sendActionToHandler(FallAlertHandlerService.ACTION_HELP)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleFallIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.start()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stop()
    }

    private fun handleFallIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_FALL_DETECTED, false) == true) {
            Log.d(TAG, "Fall intent received — showing dialog")
            fallTimestamp = intent.getLongExtra(EXTRA_FALL_TIMESTAMP, System.currentTimeMillis())
            showFallDialog.value = true
            // Clear the extra so it doesn't re-trigger on config change
            intent.removeExtra(EXTRA_FALL_DETECTED)
        }
    }

    private fun sendActionToHandler(action: String) {
        val intent = Intent(this, FallAlertHandlerService::class.java).apply {
            this.action = action
        }
        startService(intent)
    }

    private fun checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            if (!nm.canUseFullScreenIntent()) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FallDetectedDialog(
    countdownSeconds: Int = 15,
    onImOk: () -> Unit,
    onNeedHelp: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(countdownSeconds) }
    var helpSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        if (!helpSent) {
            helpSent = true
            onNeedHelp()
        }
    }

    val progress by animateFloatAsState(
        targetValue = secondsLeft.toFloat() / countdownSeconds,
        animationSpec = tween(durationMillis = 900),
        label = "countdown"
    )

    BasicAlertDialog(
        onDismissRequest = { /* non-dismissible */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A2E))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Alert icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fall Detected",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (helpSent) {
                Text(
                    text = "Help is on the way",
                    fontSize = 14.sp,
                    color = Color(0xFF81C784),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Are you okay? Help will be sent in",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Countdown ring
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(80.dp),
                        color = if (secondsLeft <= 3) Color(0xFFE53935) else Color(0xFFFF9800),
                        trackColor = Color(0xFF2C2C44),
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "$secondsLeft",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!helpSent) {
                Button(
                    onClick = {
                        helpSent = true
                        onNeedHelp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Need Help", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onImOk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I'm OK", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Text(
                    text = "Emergency contacts have been notified",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onImOk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dismiss", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

class MobileViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MobileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MobileViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}