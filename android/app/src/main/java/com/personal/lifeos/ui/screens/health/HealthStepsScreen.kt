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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.ui.components.InfiniteAuraRing
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

    var showWeightDialog by remember { mutableStateOf(false) }
    var currentWeight by remember { mutableStateOf(79.5) }
    var weightInput by remember { mutableStateOf("79.5") }

    val stepPct = if (state.stepsTarget > 0) (state.stepsCurrent.toFloat() / state.stepsTarget).coerceIn(0f, 1f) else 0f
    val distanceKm = state.stepsCurrent * 0.00075
    val caloriesBurned = state.stepsCurrent * 0.04
    val activeMinutes = state.stepsCurrent / 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health & Step Activity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDarkPrimary
                    )
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Steps Card with Infinite Aura
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = GlowSkyBlue),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HARDWARE PEDOMETER ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    InfiniteAuraRing(
                        progress = stepPct,
                        size = 170.dp,
                        strokeWidth = 14.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%,d", state.stepsCurrent),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkPrimary,
                                fontSize = 34.sp
                            )
                            Text(
                                text = "of ${String.format("%,d", state.stepsTarget)} steps",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SkyStatPill("Distance", String.format("%.2f km", distanceKm))
                        SkyStatPill("Calories", "${caloriesBurned.toInt()} kcal")
                        SkyStatPill("Active Time", "${activeMinutes} min")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                // Real-time test step increment
                                homeViewModel.repository.updateSteps(state.stepsCurrent + 250)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary, contentColor = PureWhite),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = "Step", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Walking Steps (+250)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Weight & Body Metric Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WEIGHT & TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showWeightDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Weight", tint = SkyBluePrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$currentWeight kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkPrimary
                            )
                            Text("Current Weight", style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
                        }

                        Icon(Icons.Default.ArrowForward, contentDescription = "to", tint = SkyBluePrimary)

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "75.0 kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldSuccess
                            )
                            Text("Goal Target", style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
                        }
                    }
                }
            }
        }

        if (showWeightDialog) {
            AlertDialog(
                onDismissRequest = { showWeightDialog = false },
                title = { Text("Log Current Weight", color = TextDarkPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            weightInput.toDoubleOrNull()?.let {
                                currentWeight = it
                            }
                            showWeightDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                    ) {
                        Text("Save Weight")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWeightDialog = false }) { Text("Cancel") }
                },
                containerColor = PureWhite
            )
        }
    }
}

@Composable
fun SkyStatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextDarkPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
    }
}
