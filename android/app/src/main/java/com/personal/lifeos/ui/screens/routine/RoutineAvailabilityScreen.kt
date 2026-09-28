package com.personal.lifeos.ui.screens.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.data.local.entity.RoutineBlockEntity
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAvailabilityScreen(
    onBack: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var blocks by remember { mutableStateOf<List<RoutineBlockEntity>>(emptyList()) }
    var showOutingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeViewModel.repository.getRoutineBlocks().collect {
            blocks = it
        }
    }

    val availMins = state.availability?.availableFocusedMinutes ?: 150
    val reqMins = state.availability?.requiredTaskMinutes ?: 160

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Schedule & Routines", color = Slate100, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Slate100)
                    }
                },
                actions = {
                    TextButton(onClick = { showOutingDialog = true }) {
                        Text("+ Plan Outing", color = SecondaryCyan, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        containerColor = Slate950
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Availability Engine Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "AVAILABILITY CALCULATION (LEVEL 1 DETERMINISTIC)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${availMins / 60}h ${availMins % 60}m",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentEmerald
                                )
                                Text("Available Focused Time", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${reqMins / 60}h ${reqMins % 60}m",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (availMins >= reqMins) Slate100 else AccentRose
                                )
                                Text("Required by Tasks", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val statusText = if (availMins >= reqMins) {
                            "Feasible schedule. You have a ${availMins - reqMins}m buffer."
                        } else {
                            "Conflict: ${reqMins - availMins}m shortage. Move flexible tasks to tomorrow."
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (availMins >= reqMins) AccentEmerald else AccentRose,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Text(
                    text = "FIXED ROUTINE & COMMITMENT BLOCKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(blocks, key = { it.id }) { block ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = block.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate100
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${block.category.uppercase()} • ${block.durationMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                        Text(
                            text = "${block.startTime} - ${block.endTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryCyan
                        )
                    }
                }
            }
        }

        if (showOutingDialog) {
            AlertDialog(
                onDismissRequest = { showOutingDialog = false },
                title = { Text("Plan Outing / Commitment", color = Slate100, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Add a 4-hour outing block today (e.g. 14:00 - 18:00)? The availability engine will automatically recalculate your remaining time.",
                        color = Slate400
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                homeViewModel.repository.addRoutineBlock(
                                    RoutineBlockEntity(
                                        name = "Outing / Event",
                                        category = "outing",
                                        startTime = "14:00",
                                        endTime = "18:00",
                                        durationMinutes = 240,
                                        dayType = "weekday"
                                    )
                                )
                                showOutingDialog = false
                            }
                        }
                    ) {
                        Text("Confirm Outing")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOutingDialog = false }) { Text("Cancel") }
                },
                containerColor = CardBackground
            )
        }
    }
}
