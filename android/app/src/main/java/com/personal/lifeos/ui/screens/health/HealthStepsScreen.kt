package com.personal.lifeos.ui.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.ui.components.ProgressRing
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthStepsScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val stepPct = (state.stepsCurrent.toFloat() / state.stepsTarget).coerceIn(0f, 1f)
    val distanceKm = state.stepsCurrent * 0.00075
    val caloriesBurned = state.stepsCurrent * 0.04
    val activeMinutes = state.stepsCurrent / 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health & Activity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Steps Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProgressRing(
                        progress = stepPct,
                        size = 160.dp,
                        strokeWidth = 14.dp,
                        primaryColor = SecondaryCyan,
                        secondaryColor = AccentEmerald
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%,d", state.stepsCurrent),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "of ${String.format("%,d", state.stepsTarget)} steps",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatPill("Distance", String.format("%.1f km", distanceKm))
                        StatPill("Calories", "${caloriesBurned.toInt()} kcal")
                        StatPill("Active", "${activeMinutes} min")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                // Add 500 steps simulation or Health Connect sync
                                homeViewModel.repository.updateSteps(state.stepsCurrent + 500)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryCyan)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Slate950)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Health Connect (+500 steps)", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Weight Tracking Card (Section 20)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BODY WEIGHT PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "79.5 kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text("Current Weight", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        }

                        Icon(Icons.Default.ArrowForward, contentDescription = "to", tint = Slate400)

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "75.0 kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                            Text("Target Goal", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Trend: -1.7 kg over last 30 days. On healthy pace.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentEmerald
                    )
                }
            }

            // Weekly Step Insights (Section 15 & 28)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "WEEKLY STEP CONSISTENCY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Average: 9,774 steps/day • Best Day: 11,200 steps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate100
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate100)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate400)
    }
}
