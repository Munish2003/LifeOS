package com.personal.lifeos.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.data.remote.DirectAiClient
import com.personal.lifeos.ui.components.InfiniteAuraRing
import com.personal.lifeos.ui.components.SkyMetricCard
import com.personal.lifeos.ui.components.SkyNextActionCard
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onNavigateToTimer: (String) -> Unit,
    onNavigateToSchedule: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    val dateFormatted = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good Day 👋",
                            style = MaterialTheme.typography.titleMedium,
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDarkPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSchedule) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Schedule",
                            tint = SkyBluePrimary
                        )
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextDarkSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SkyBackground
                )
            )
        },
        containerColor = SkyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Main Hero Card: Overall Daily Progress with Infinite Rotating Aura
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(26.dp), ambientColor = GlowSkyBlue),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
            ) {
                Row(
                    modifier = Modifier.padding(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DAILY LIFE SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(state.overallProgressPct * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDarkPrimary,
                            fontSize = 38.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val availMins = state.availability?.availableFocusedMinutes ?: 150
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SkyBlueSurface
                        ) {
                            Text(
                                text = "${availMins / 60}h ${availMins % 60}m usable focus time",
                                style = MaterialTheme.typography.labelSmall,
                                color = SkyBluePrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Rotating Aura Progress Ring
                    InfiniteAuraRing(
                        progress = state.overallProgressPct,
                        size = 110.dp,
                        strokeWidth = 10.dp
                    ) {
                        Text(
                            text = "${(state.overallProgressPct * 100).toInt()}%",
                            fontWeight = FontWeight.ExtraBold,
                            color = SkyBluePrimary,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Conflict Warning Banner if day is overloaded
            state.availability?.conflictMessage?.let { conflict ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AmberWarning, PureWhite)))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Conflict",
                            tint = AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SCHEDULE NOTICE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = conflict,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Next Action Card with continuous breathing glow
            val nextTask = state.availability?.recommendedTask ?: state.tasks.firstOrNull { !it.isCompleted }
            val nextTitle = nextTask?.title ?: "Python Deep Work"
            val nextRemMins = if (nextTask != null && nextTask.measurementType == "time_based") {
                kotlin.math.max(1, (nextTask.targetValue - nextTask.currentValue).toInt())
            } else 45

            SkyNextActionCard(
                taskTitle = nextTitle,
                remainingMinutes = nextRemMins,
                reason = state.availability?.recommendedReason
                    ?: "You have an available focus window right now. Start a session to make meaningful progress.",
                onStartTimer = { onNavigateToTimer(nextTitle) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "REAL-TIME TRACKING",
                style = MaterialTheme.typography.labelSmall,
                color = SkyBluePrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Steps Metric Card (Google Fit / Real hardware sensor)
            val isFitLinked = remember { viewModel.googleFitManager.isConnected() }
            SkyMetricCard(
                title = if (isFitLinked) "Google Fit Steps" else "Daily Steps",
                currentFormatted = String.format("%,d", state.stepsCurrent),
                targetFormatted = String.format("%,d", state.stepsTarget),
                progress = if (state.stepsTarget > 0) state.stepsCurrent.toFloat() / state.stepsTarget else 0f,
                icon = {
                    Icon(
                        if (isFitLinked) Icons.Default.DirectionsRun else Icons.Default.DirectionsWalk,
                        contentDescription = "Steps",
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                },
                accentColor = CyanAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Calories Metric Card (Real Food Logged)
            SkyMetricCard(
                title = "Diet & Calories",
                currentFormatted = "${state.caloriesCurrent.toInt()} kcal",
                targetFormatted = "${state.caloriesTarget.toInt()} kcal",
                progress = if (state.caloriesTarget > 0) (state.caloriesCurrent.toFloat() / state.caloriesTarget.toFloat()) else 0f,
                icon = {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = "Calories",
                        tint = AmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                },
                accentColor = AmberWarning
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Focus Time Metric Card (Real Timer Sessions)
            val currH = state.focusMinutesCurrent / 60
            val currM = state.focusMinutesCurrent % 60
            val targH = state.focusMinutesTarget / 60
            val targM = state.focusMinutesTarget % 60
            SkyMetricCard(
                title = "Focused Study & Deep Work",
                currentFormatted = "${currH}h ${currM}m",
                targetFormatted = if (targM == 0) "${targH}h" else "${targH}h ${targM}m",
                progress = if (state.focusMinutesTarget > 0) state.focusMinutesCurrent.toFloat() / state.focusMinutesTarget else 0f,
                icon = {
                    Icon(
                        Icons.Default.Computer,
                        contentDescription = "Focus",
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                accentColor = SkyBluePrimary
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Settings Dialog for custom Server IP and Hugging Face API key
        if (showSettingsDialog) {
            val context = androidx.compose.ui.platform.LocalContext.current
            var hfKey by remember { mutableStateOf(DirectAiClient.getHfKey(context)) }
            var serverUrl by remember { mutableStateOf(DirectAiClient.getServerUrl(context)) }

            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("App & AI Connection Settings", color = TextDarkPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Mobile AI seedhe Hugging Face cloud ya aapke backend computer se connect ho sakti hai:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                        OutlinedTextField(
                            value = hfKey,
                            onValueChange = { hfKey = it },
                            label = { Text("Hugging Face API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("Backend URL (e.g. http://192.168.1.106:8000/)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            DirectAiClient.saveHfKey(context, hfKey)
                            DirectAiClient.saveServerUrl(context, serverUrl)
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                    ) {
                        Text("Save & Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) { Text("Cancel") }
                },
                containerColor = PureWhite
            )
        }
    }
}
