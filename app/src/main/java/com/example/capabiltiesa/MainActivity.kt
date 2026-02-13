package com.example.capabiltiesa

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.capabiltiesa.ui.theme.CapabiltiesaTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

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
            Log.d(TAG, "Fall intent received — showing dialog over lock screen")
            fallTimestamp = intent.getLongExtra(EXTRA_FALL_TIMESTAMP, System.currentTimeMillis())
            showFallDialog.value = true
            showOverLockScreen()
            // Clear the extra so it doesn't re-trigger on config change
            intent.removeExtra(EXTRA_FALL_DETECTED)
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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


@Composable
fun PhoneSearchAnimation(
    modifier: Modifier = Modifier,
    phoneColor: Color = Color.White,
    dotColor: Color = Color.White, // yellowish dot
    backgroundColor: Color = Color.Black,
    animationDurationMs: Int = 2800
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbit")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )

    Canvas(modifier = modifier.background(backgroundColor)) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Phone icon dimensions
        val phoneWidth = size.width * 0.28f
        val phoneHeight = size.height * 0.48f
        val cornerRadius = phoneWidth * 0.22f

        val phoneRect = RoundRect(
            left = centerX - phoneWidth / 2,
            top = centerY - phoneHeight / 2,
            right = centerX + phoneWidth / 2,
            bottom = centerY + phoneHeight / 2,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        // Draw phone outline
        drawPath(
            path = Path().apply { addRoundRect(phoneRect) },
            color = phoneColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Orbit path — a rounded rect slightly larger than the phone
// Orbit path — SAME as the phone outline (dot sits on the border)
        val orbitWidth = phoneWidth / 2
        val orbitHeight = phoneHeight / 2
        val orbitCorner = cornerRadius

        val dotRadius = 14.dp.toPx() // slightly larger dot for the half-in half-out look

        val dotOffset = getPointOnRoundedRect(
            cx = centerX,
            cy = centerY,
            halfW = orbitWidth,
            halfH = orbitHeight,
            cornerR = orbitCorner,
            angleDeg = angle
        )
        drawCircle(
            color = dotColor,
            radius = dotRadius,
            center = dotOffset
        )
    }
}

/**
 * Returns a point on a rounded rectangle's perimeter at the given angle (degrees).
 * 0° = top-center, clockwise.
 */
fun getPointOnRoundedRect(
    cx: Float, cy: Float,
    halfW: Float, halfH: Float,
    cornerR: Float, angleDeg: Float
): Offset {
    // Clamp corner radius
    val r = cornerR.coerceAtMost(minOf(halfW, halfH))

    // Total perimeter segments
    val straightH = 2 * (halfH - r) // each vertical straight
    val straightW = 2 * (halfW - r) // each horizontal straight
    val arcLen = (Math.PI.toFloat() / 2) * r // quarter circle arc length
    val totalPerimeter = 2 * straightW + 2 * straightH + 4 * arcLen

    // Map angle (0°=top center, CW) to distance along perimeter
    val fraction = (angleDeg % 360f) / 360f
    var d = fraction * totalPerimeter

    // Segment order starting from top-center going clockwise:
    // 1. top straight right half
    // 2. top-right arc
    // 3. right straight
    // 4. bottom-right arc
    // 5. bottom straight
    // 6. bottom-left arc
    // 7. left straight
    // 8. top-left arc
    // 9. top straight left half

    val topHalf = straightW / 2

    // 1 — Top edge, center → right
    if (d <= topHalf) {
        return Offset(cx + d, cy - halfH)
    }
    d -= topHalf

    // 2 — Top-right corner arc
    if (d <= arcLen) {
        val a = d / r // angle in radians along arc
        return Offset(
            cx + halfW - r + r * sin(a),
            cy - halfH + r - r * cos(a)
        )
    }
    d -= arcLen

    // 3 — Right edge
    if (d <= straightH) {
        return Offset(cx + halfW, cy - halfH + r + d)
    }
    d -= straightH

    // 4 — Bottom-right corner arc
    if (d <= arcLen) {
        val a = d / r
        return Offset(
            cx + halfW - r + r * cos(a),
            cy + halfH - r + r * sin(a)
        )
    }
    d -= arcLen

    // 5 — Bottom edge (right → left)
    if (d <= straightW) {
        return Offset(cx + halfW - r - d, cy + halfH)
    }
    d -= straightW

    // 6 — Bottom-left corner arc
    if (d <= arcLen) {
        val a = d / r
        return Offset(
            cx - halfW + r - r * sin(a),
            cy + halfH - r + r * cos(a)
        )
    }
    d -= arcLen

    // 7 — Left edge (bottom → top)
    if (d <= straightH) {
        return Offset(cx - halfW, cy + halfH - r - d)
    }
    d -= straightH

    // 8 — Top-left corner arc
    if (d <= arcLen) {
        val a = d / r
        return Offset(
            cx - halfW + r - r * cos(a),
            cy - halfH + r - r * sin(a)
        )
    }
    d -= arcLen

    // 9 — Top edge left half (left → center)
    return Offset(cx - halfW + r + d, cy - halfH)
}

@Preview
@Composable
private fun Demo() {
    Box(Modifier.fillMaxSize()) {
        PhoneSearchAnimation(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
        )
    }

}