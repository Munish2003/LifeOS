package com.personal.lifeos.ui.screens.timer

import android.content.Context
import android.content.Intent
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

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(taskTitle, color = Slate100, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Slate100)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        containerColor = Slate950
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
                Text(
                    text = "DEEP FOCUS SESSION",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryLight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Stay uninterrupted. Background tracking active.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            }

            // Big Timer Display
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isRunning) "SESSION IN PROGRESS" else "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRunning) AccentEmerald else Slate400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controls
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
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Slate100)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause", color = Slate100, fontWeight = FontWeight.Bold)
                    }
                }

                if (elapsedSeconds > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            val savedSeconds = elapsedSeconds
                            sendTimerAction(TimerService.ACTION_STOP)
                            scope.launch {
                                // Save session to database and update task
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
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
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
