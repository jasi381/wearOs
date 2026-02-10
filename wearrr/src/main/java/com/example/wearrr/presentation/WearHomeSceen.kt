package com.example.wearrr.presentation

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearrr.R

@Composable
fun WearHomeScreen(viewModel: WearViewModel) {
    val state by viewModel.uiState.collectAsState()


    when (state) {
        WearUiState.Checking -> {
            WearLoading("Checking phone…")
        }

        WearUiState.PhoneNotInstalled -> {
            WearError("Install phone app to enable Fall Detection")
        }

        WearUiState.Connected -> {
            WearSuccess("Monitoring Active ✅")
        }
    }
}


@Composable
fun WearBaseScreen(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: androidx.compose.ui.graphics.Color
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
                color = androidx.compose.ui.graphics.Color.Gray
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
            trackColor = androidx.compose.ui.graphics.Color.Red
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
fun WearError(message: String) {
    WearBaseScreen(
        icon = ImageVector.vectorResource(R.drawable.ic_warn),
        title = "Phone App Missing",
        subtitle = message,
        iconTint =androidx.compose.ui.graphics.Color.Red
    )
}

@Composable
fun WearSuccess(message: String) {
    WearBaseScreen(
        icon =  ImageVector.vectorResource(R.drawable.ic_check),
        title = "Fall Detection",
        subtitle = message,
        iconTint =androidx.compose.ui.graphics.Color(0xFF4CAF50)
    )
}


