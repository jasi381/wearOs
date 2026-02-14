package com.example.wearrr.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import coil.compose.rememberAsyncImagePainter
import com.example.wearrr.R
import com.example.wearrr.fall.FallDetectionService
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WearHomeScreen(viewModel: WearViewModel) {
    val state by viewModel.uiState.collectAsState()
    val message by viewModel.receivedMessage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    when (state) {
        WearUiState.Checking -> {
            WearLoading("Checking phone…")
        }

        WearUiState.PhoneNotInstalled -> {
            WearError("Install phone app to enable Fall Detection")
        }

        WearUiState.Disconnected -> {
            WearDisconnected()
        }

        WearUiState.Connected -> {
            if (!isLoggedIn) {
                WaitingForLoginScreen()
            } else if (message != null) {
                WearMessageScreen(message!!)
            } else {
                WearSuccess("Monitoring Active")
            }
        }
    }
}

@Composable
fun WaitingForLoginScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WatchPhoneSearchAnimation(
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Continue on Mobile",
            style = MaterialTheme.typography.title3,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Sign in to activate",
            style = MaterialTheme.typography.body2,
            color = Color(0xFF7F8C8D),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WatchPhoneSearchAnimation(
    modifier: Modifier = Modifier,
    phoneColor: Color = Color.White,
    dotColor: Color = Color(0xFFE94560),
    animationDurationMs: Int = 2500
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

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Phone icon dimensions - smaller for watch
        val phoneWidth = size.width * 0.32f
        val phoneHeight = size.height * 0.52f
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

        // Orbit path
        val orbitWidth = phoneWidth / 2
        val orbitHeight = phoneHeight / 2
        val orbitCorner = cornerRadius

        val dotRadius = 8.dp.toPx()

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
private fun getPointOnRoundedRect(
    cx: Float, cy: Float,
    halfW: Float, halfH: Float,
    cornerR: Float, angleDeg: Float
): Offset {
    val r = cornerR.coerceAtMost(minOf(halfW, halfH))

    val straightH = 2 * (halfH - r)
    val straightW = 2 * (halfW - r)
    val arcLen = (Math.PI.toFloat() / 2) * r
    val totalPerimeter = 2 * straightW + 2 * straightH + 4 * arcLen

    val fraction = (angleDeg % 360f) / 360f
    var d = fraction * totalPerimeter

    val topHalf = straightW / 2

    // Top edge, center to right
    if (d <= topHalf) {
        return Offset(cx + d, cy - halfH)
    }
    d -= topHalf

    // Top-right corner arc
    if (d <= arcLen) {
        val a = d / r
        return Offset(
            cx + halfW - r + r * sin(a),
            cy - halfH + r - r * cos(a)
        )
    }
    d -= arcLen

    // Right edge
    if (d <= straightH) {
        return Offset(cx + halfW, cy - halfH + r + d)
    }
    d -= straightH

    // Bottom-right corner arc
    if (d <= arcLen) {
        val a = d / r
        return Offset(
            cx + halfW - r + r * cos(a),
            cy + halfH - r + r * sin(a)
        )
    }
    d -= arcLen

    // Bottom edge
    if (d <= straightW) {
        return Offset(cx + halfW - r - d, cy + halfH)
    }
    d -= straightW

    // Bottom-left corner arc
    if (d <= arcLen) {
        val a = d / r
        return Offset(
            cx - halfW + r - r * sin(a),
            cy + halfH - r + r * cos(a)
        )
    }
    d -= arcLen

    // Left edge
    if (d <= straightH) {
        return Offset(cx - halfW, cy + halfH - r - d)
    }
    d -= straightH

    // Top-left corner arc
    if (d <= arcLen) {
        val a = d / r
        return Offset(
            cx - halfW + r - r * cos(a),
            cy - halfH + r - r * sin(a)
        )
    }
    d -= arcLen

    // Top edge left half
    return Offset(cx - halfW + r + d, cy - halfH)
}

@Composable
fun WearMessageScreen(message: com.example.wearrr.DummyMessage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!message.photoUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(message.photoUrl),
                contentDescription = "Profile photo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = message.name,
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WearBaseScreen(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun WearLoading(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp,
            trackColor = Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WearDisconnected() {
    WearBaseScreen(
        icon = ImageVector.vectorResource(R.drawable.ic_warn),
        title = "Phone Disconnected",
        subtitle = "Turn on Bluetooth and keep devices nearby",
        iconTint = Color(0xFFFF9800)
    )
}

@Composable
fun WearError(message: String) {
    WearBaseScreen(
        icon = ImageVector.vectorResource(R.drawable.ic_warn),
        title = "Phone App Missing",
        subtitle = message,
        iconTint = Color.Red
    )
}

@Composable
fun WearSuccess(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_check),
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Fall Detection",
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { FallDetectionService.simulateFall() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F)),
            modifier = Modifier.size(width = 120.dp, height = 36.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Simulate Fall", fontSize = 12.sp, color = Color.White)
        }
    }
}
