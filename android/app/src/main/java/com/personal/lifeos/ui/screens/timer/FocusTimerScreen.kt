package com.personal.lifeos.ui.screens.timer

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.service.TimerService
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    taskTitle: String,
    onBack: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val elapsedSeconds by TimerService.timerState.collectAsState()
    val isRunning by TimerService.isRunningState.collectAsState()

    fun sendTimerAction(action: String) {
        val intent = Intent(context, TimerService::class.java).apply {
            this.action = action
            putExtra(TimerService.EXTRA_TASK_TITLE, taskTitle)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    var targetMinutes by remember { mutableStateOf(25) }
    val remainingSeconds = maxOf(0, targetMinutes * 60 - elapsedSeconds)

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    // Continuous Infinite Breathing Wave when timer is running
    val infiniteTransition = rememberInfiniteTransition(label = "TimerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TimerPulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(taskTitle, color = TextDarkPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SkyBluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBackground)
            )
        },
        containerColor = SkyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SkyBlueSurface
                ) {
                    Text(
                        text = if (isRunning) "ACTIVE DEEP FOCUS" else "TIMER READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Background service active. Locks safely with screen off.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDarkSecondary
                )
            }

            // Big Animated Stopwatch Dial
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(pulseScale)
                    .shadow(elevation = 8.dp, shape = CircleShape, spotColor = GlowSkyBlue)
                    .clip(CircleShape)
                    .background(PureWhite),
                contentAlignment = Alignment.Center
            ) {
                // Circular Ring track
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    drawCircle(
                        color = SkyBlueSurface,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    if (isRunning || elapsedSeconds > 0) {
                        val totalSeconds = targetMinutes * 60
                        val sweep = (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f) * 360f
                        drawArc(
                            brush = Brush.sweepGradient(listOf(SkyBluePrimary, CyanAccent, SkyBlueLight)),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = TextDarkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isRunning) "TRACKING..." else "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRunning) EmeraldSuccess else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Presets and Meta
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PresetButton("25m", targetMinutes == 25) { targetMinutes = 25 }
                    PresetButton("45m", targetMinutes == 45) { targetMinutes = 45 }
                    PresetButton("60m", targetMinutes == 60) { targetMinutes = 60 }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Computer, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deep Work", style = MaterialTheme.typography.labelSmall, color = TextDarkPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", color = TextMuted)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Headphones, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Focus Chamber Active", style = MaterialTheme.typography.labelSmall, color = TextDarkPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isRunning) {
                    Button(
                        onClick = { sendTimerAction(TimerService.ACTION_START) },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyBluePrimary,
                            contentColor = PureWhite
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Timer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { sendTimerAction(TimerService.ACTION_PAUSE) },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBluePrimary)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = SkyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause", color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                    }
                }

                if (elapsedSeconds > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            val savedSeconds = elapsedSeconds
                            sendTimerAction(TimerService.ACTION_STOP)
                            scope.launch {
                                val task = homeViewModel.uiState.value.tasks.find { it.title == taskTitle }
                                if (task != null) {
                                    homeViewModel.repository.recordSession(task.id, savedSeconds)
                                }
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = PureWhite
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Finish")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PresetButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) SkyBluePrimary else PureWhite,
        border = if (!isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, Color.Transparent))) else null,
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) PureWhite else TextDarkPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
