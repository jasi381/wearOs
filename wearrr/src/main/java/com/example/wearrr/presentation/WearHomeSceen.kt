package com.example.wearrr.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
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

@Composable
fun WearHomeScreen(viewModel: WearViewModel) {
    val state by viewModel.uiState.collectAsState()
    val message by viewModel.receivedMessage.collectAsState()

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
            if (message != null) {
                WearMessageScreen(message!!)
            } else {
                WearSuccess("Monitoring Active")
            }
        }
    }
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
