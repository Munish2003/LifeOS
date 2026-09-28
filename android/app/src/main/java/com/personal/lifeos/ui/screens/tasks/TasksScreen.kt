package com.personal.lifeos.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.data.local.entity.TaskEntity
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToTimer: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = PrimaryLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryIndigo,
                contentColor = Slate100
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = Slate950
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "CATEGORIZED WORKFLOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(state.tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleComplete = {
                        scope.launch {
                            homeViewModel.repository.updateTask(
                                task.copy(isCompleted = !task.isCompleted)
                            )
                        }
                    },
                    onStartTimer = {
                        onNavigateToTimer(task.title)
                    },
                    onIncrementProgress = { amount ->
                        scope.launch {
                            val newProgress = task.currentValue + amount
                            val completed = newProgress >= task.targetValue && task.targetValue > 0
                            homeViewModel.repository.updateTask(
                                task.copy(currentValue = newProgress, isCompleted = completed)
                            )
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, category, type, target, unit, isFlexible ->
                    scope.launch {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        homeViewModel.repository.addTask(
                            TaskEntity(
                                title = title,
                                category = category,
                                measurementType = type,
                                targetValue = target,
                                currentValue = 0.0,
                                unit = unit,
                                priority = 2,
                                scheduledDate = today,
                                isFlexible = isFlexible
                            )
                        )
                        showAddDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onStartTimer: () -> Unit,
    onIncrementProgress: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Slate900 else CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentEmerald,
                    uncheckedColor = Slate700
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) Slate400 else Slate100
                )
                Spacer(modifier = Modifier.height(4.dp))

                val progressText = when (task.measurementType) {
                    "time_based" -> {
                        val currH = task.currentValue.toInt() / 60
                        val currM = task.currentValue.toInt() % 60
                        val targH = task.targetValue.toInt() / 60
                        val targM = task.targetValue.toInt() % 60
                        "${currH}h ${currM}m / ${targH}h ${targM}m"
                    }
                    "quantity_based" -> "${task.currentValue.toInt()} / ${task.targetValue.toInt()} ${task.unit}"
                    "binary" -> if (task.isCompleted) "Completed" else "Pending"
                    else -> "${task.currentValue.toInt()} / ${task.targetValue.toInt()} ${task.unit}"
                }

                Text(
                    text = "${task.category.uppercase()} • $progressText",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            // Quick actions based on type
            when (task.measurementType) {
                "time_based" -> {
                    IconButton(
                        onClick = onStartTimer,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Start Timer",
                            tint = PrimaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                "quantity_based" -> {
                    Row {
                        IconButton(
                            onClick = { onIncrementProgress(1.0) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SecondaryCyan.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add 1", tint = SecondaryCyan)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, type: String, target: Double, unit: String, isFlexible: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("learning") }
    var type by remember { mutableStateOf("time_based") }
    var targetStr by remember { mutableStateOf("60") }
    var isFlexible by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task", color = Slate100, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Python, DSA)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text(if (type == "time_based") "Target (Minutes)" else "Target Count") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFlexible, onCheckedChange = { isFlexible = it })
                    Text("Flexible schedule (can shift if day is tight)", color = Slate400, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull() ?: 60.0
                    val unit = if (type == "time_based") "minutes" else "count"
                    onAdd(title, category, type, target, unit, isFlexible)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = CardBackground
    )
}
