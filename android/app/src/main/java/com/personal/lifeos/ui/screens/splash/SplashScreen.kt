package com.personal.lifeos.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.lifeos.ui.theme.SkyBackground
import com.personal.lifeos.ui.theme.SkyBluePrimary
import com.personal.lifeos.ui.theme.SkyBlueVibrant
import com.personal.lifeos.ui.theme.TextDarkPrimary
import com.personal.lifeos.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var text by remember { mutableStateOf("Calibrating system...") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        // Simulate loading sequence
        delay(500)
        progress = 0.3f
        text = "Syncing with sensors..."
        delay(800)
        progress = 0.7f
        text = "Loading daily intelligence..."
        delay(800)
        progress = 1.0f
        text = "Ready."
        delay(400)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBackground),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SkyBluePrimary.copy(alpha = 0.15f), Color.Transparent),
                    radius = size.minDimension * 0.8f
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Pulse Emblem
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Box(
                    modifier = Modifier
                        .size((60 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(SkyBlueVibrant.copy(alpha = pulseAlpha))
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SkyBluePrimary, Color(0xFF1E3A8A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LIFE OS",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextDarkPrimary,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Autonomous Personal Intelligence",
                style = MaterialTheme.typography.bodyMedium,
                color = SkyBluePrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Hairline Progress Bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(SkyBluePrimary, SkyBlueVibrant)))
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}
