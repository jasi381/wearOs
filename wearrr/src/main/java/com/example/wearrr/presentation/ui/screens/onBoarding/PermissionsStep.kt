package com.example.wearrr.presentation.ui.screens.onBoarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.wearrr.R
import com.example.wearrr.presentation.ui.utils.clickableNoRipple
import com.example.wearrr.presentation.theme.FallGuardColors

/**
 * Created by Jasmeet Singh on 03/12/25.
 */


/**
 * Permission Step - Simple and clear
 */
@Preview(
    device = "id:wearos_large_round",
    showSystemUi = true,
    backgroundColor = 0xFF0A0E27
)
@Composable
fun PermissionStep(
    navController: NavHostController? = null,
    onRequestPermission: () -> Unit = {}
) {


    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff000719))
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(top = 4.dp),
        autoCentering = null  // Disable auto-centering
    ) {
        item {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.layer_1),
                contentDescription = "Permission Required",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(52.dp)
                    .background(Color(0xff002C2F), CircleShape)
                    .clip(CircleShape)
                    .padding(8.dp),
                tint = Color(0xff00D498)


            )
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            Text(
                text = "Enable Monitoring",
                style = MaterialTheme.typography.titleMedium,
                color = FallGuardColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        item { Spacer(modifier = Modifier.height(4.dp)) }


        item {
            Text(
                text = "We need sensor access to\ndetect falls",
                style = MaterialTheme.typography.bodyMedium,
                color = FallGuardColors.textSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
        item { Spacer(modifier = Modifier.height(18.dp)) }


        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(FallGuardColors.warning, MaterialTheme.shapes.large)
                    .padding(vertical = 6.dp, horizontal = 20.dp)
                    .clickableNoRipple(
                        onClick = onRequestPermission,
                        hapticFeedback = true,
                        role = Role.Button
                    )
                    .clip(MaterialTheme.shapes.large)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_forward),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color(0xff333847), MaterialTheme.shapes.large)
                    .padding(vertical = 6.dp, horizontal = 30.dp)
                    .clickableNoRipple(
                        onClick = { navController?.popBackStack() },
                        hapticFeedback = true,
                        role = Role.Button
                    )
                    .clip(MaterialTheme.shapes.large)
            ) {

                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

            }
        }
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

}