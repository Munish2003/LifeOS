package com.personal.lifeos.ui.screens.tasks

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    var currentFilter by remember { mutableStateOf("all") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tasks & Productivity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDarkPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Task", tint = SkyBluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SkyBluePrimary,
                contentColor = PureWhite,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = SkyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TaskFilterTabs(
                currentFilter = currentFilter,
                onFilterSelected = { currentFilter = it },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
            val filteredTasks = state.tasks.filter { task ->
                when (currentFilter) {
                    "P1" -> task.priority == 1
                    "P2" -> task.priority == 2
                    "P3" -> task.priority == 3
                    else -> true
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S ACTION ITEMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "${state.tasks.count { it.isCompleted }} of ${state.tasks.size} done",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDarkSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(filteredTasks, key = { it.id }) { task ->
                SkyTaskCard(
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
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showAddDialog) {
            AddSkyTaskDialog(
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
                                priority = 1,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilterTabs(currentFilter: String, onFilterSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == "all",
            onClick = { onFilterSelected("all") },
            label = { Text("📋 All") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = PureWhite,
                selectedContainerColor = SkyBlueSurface,
                selectedLabelColor = SkyBluePrimary
            ),
            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentFilter == "all", borderColor = CardBorderLight)
        )
        FilterChip(
            selected = currentFilter == "P1",
            onClick = { onFilterSelected("P1") },
            label = { Text("🔥 P1") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = PureWhite,
                selectedContainerColor = RoseError.copy(alpha=0.15f),
                selectedLabelColor = RoseError
            ),
            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentFilter == "P1", borderColor = CardBorderLight)
        )
        FilterChip(
            selected = currentFilter == "P2",
            onClick = { onFilterSelected("P2") },
            label = { Text("⚡ P2") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = PureWhite,
                selectedContainerColor = AmberWarning.copy(alpha=0.15f),
                selectedLabelColor = AmberWarning
            ),
            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentFilter == "P2", borderColor = CardBorderLight)
        )
        FilterChip(
            selected = currentFilter == "P3",
            onClick = { onFilterSelected("P3") },
            label = { Text("📌 P3") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = PureWhite,
                selectedContainerColor = SkyBlueSurface,
                selectedLabelColor = SkyBluePrimary
            ),
            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = currentFilter == "P3", borderColor = CardBorderLight)
        )
    }
}

@Composable
fun SkyTaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onStartTimer: () -> Unit,
    onIncrementProgress: (Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), ambientColor = GlowSkyBlue),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) OffWhite else PureWhite
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (task.isCompleted) listOf(DividerColor, PureWhite)
                else listOf(CardBorderLight, PureWhite)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldSuccess,
                    uncheckedColor = SkyBluePrimary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) TextMuted else TextDarkPrimary
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
                    color = if (task.isCompleted) TextMuted else SkyBluePrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            when (task.measurementType) {
                "time_based" -> {
                    IconButton(
                        onClick = onStartTimer,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SkyBlueSurface)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Start Timer",
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                "quantity_based" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onIncrementProgress(1.0) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SkyBlueSurface)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = SkyBluePrimary)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AddSkyTaskDialog(
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
        title = { Text("Add Personal Task", color = TextDarkPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Python, DSA, Walk)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text(if (type == "time_based") "Target Duration (Minutes)" else "Target Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isFlexible,
                        onCheckedChange = { isFlexible = it },
                        colors = CheckboxDefaults.colors(checkedColor = SkyBluePrimary)
                    )
                    Text("Flexible (Auto-shift if schedule is tight)", color = TextDarkSecondary, fontSize = 12.sp)
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
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextDarkSecondary) }
        },
        containerColor = PureWhite
    )
}
