package com.personal.lifeos.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.ui.components.MetricCard
import com.personal.lifeos.ui.components.NextActionCard
import com.personal.lifeos.ui.components.ProgressRing
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

    val dateFormatted = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good Morning 👋",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate400
                        )
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSchedule) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Schedule",
                            tint = Slate100
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950
                )
            )
        },
        containerColor = Slate950
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Overall Daily Progress Ring Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Daily Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(state.overallProgressPct * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val availMins = state.availability?.availableFocusedMinutes ?: 150
                        Text(
                            text = "${availMins / 60}h ${availMins % 60}m realistic time free",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentEmerald,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    ProgressRing(
                        progress = state.overallProgressPct,
                        size = 96.dp,
                        strokeWidth = 10.dp,
                        primaryColor = PrimaryIndigo,
                        secondaryColor = SecondaryCyan
                    ) {
                        Text(
                            text = "${(state.overallProgressPct * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = Slate100,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conflict Warning Banner if day is overloaded (Section 9, 22, 47)
            state.availability?.conflictMessage?.let { conflict ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentRose.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Conflict",
                            tint = AccentRose,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SCHEDULE CONFLICT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentRose
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = conflict,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate100
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Next Action Card (Section 24)
            val nextTask = state.availability?.recommendedTask
            val nextTitle = nextTask?.title ?: "Python Deep Work"
            val nextRemMins = if (nextTask != null && nextTask.measurementType == "time_based") {
                (nextTask.targetValue - nextTask.currentValue).toInt()
            } else 43

            NextActionCard(
                taskTitle = nextTitle,
                remainingMinutes = nextRemMins,
                reason = state.availability?.recommendedReason
                    ?: "You have a free focused slot. Python is due today and has 43 minutes remaining.",
                onStartTimer = { onNavigateToTimer(nextTitle) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TODAY'S METRICS",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Steps Metric Card (Section 6 & 15)
            MetricCard(
                title = "Steps",
                currentFormatted = String.format("%,d", state.stepsCurrent),
                targetFormatted = String.format("%,d", state.stepsTarget),
                progress = state.stepsCurrent.toFloat() / state.stepsTarget,
                icon = {
                    Icon(
                        Icons.Default.DirectionsWalk,
                        contentDescription = "Steps",
                        tint = SecondaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                },
                accentColor = SecondaryCyan
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Calories Metric Card (Section 6 & 17)
            MetricCard(
                title = "Calories",
                currentFormatted = "${state.caloriesCurrent.toInt()} kcal",
                targetFormatted = "${state.caloriesTarget.toInt()} kcal",
                progress = (state.caloriesCurrent.toFloat() / state.caloriesTarget.toFloat()),
                icon = {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = "Calories",
                        tint = AccentAmber,
                        modifier = Modifier.size(20.dp)
                    )
                },
                accentColor = AccentAmber
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Focus Time Metric Card (Section 6 & 14)
            val currH = state.focusMinutesCurrent / 60
            val currM = state.focusMinutesCurrent % 60
            val targH = state.focusMinutesTarget / 60
            val targM = state.focusMinutesTarget % 60
            MetricCard(
                title = "Focus Time",
                currentFormatted = "${currH}h ${currM}m",
                targetFormatted = if (targM == 0) "${targH}h" else "${targH}h ${targM}m",
                progress = state.focusMinutesCurrent.toFloat() / state.focusMinutesTarget,
                icon = {
                    Icon(
                        Icons.Default.Computer,
                        contentDescription = "Focus",
                        tint = PrimaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                },
                accentColor = PrimaryIndigo
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
