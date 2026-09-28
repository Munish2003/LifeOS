package com.personal.lifeos.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
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
    var priority by remember { mutableStateOf("P2") }
    var targetMins by remember { mutableStateOf("45") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SkyBlueSurface,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Create New Task", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDarkSecondary)
                    }
                }

                // Task Title
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanAccent))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Task Title", style = MaterialTheme.typography.labelMedium, color = TextDarkSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Solve LeetCode DP Problems") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = CardBorderLight
                        )
                    )
                }

                // Priority Level
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B))) // Amber
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Priority Level", style = MaterialTheme.typography.labelMedium, color = TextDarkSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val priorities = listOf("P1" to "🚨 P1 Critical", "P2" to "⚡ P2 High", "P3" to "☕ P3 Normal")
                        priorities.forEach { (key, label) ->
                            val isSelected = priority == key
                            Surface(
                                onClick = { priority = key },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) SkyBlueSurface else PureWhite,
                                border = if (!isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardBorderLight, Color.Transparent))) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SkyBluePrimary else TextMuted,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Target Duration
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF8B5CF6))) // Purple
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Target Duration", style = MaterialTheme.typography.labelMedium, color = TextDarkSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("15", "25", "45", "60", "90").forEach { mins ->
                            val isSelected = targetMins == mins
                            Surface(
                                onClick = { targetMins = mins },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SkyBluePrimary else SkyBlueSurface,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp)
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else SkyBluePrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = targetMins,
                        onValueChange = { targetMins = it.filter { char -> char.isDigit() } },
                        trailingIcon = { Text("mins", color = TextMuted, modifier = Modifier.padding(end = 16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = CardBorderLight
                        )
                    )
                }

                // Create Button
                Button(
                    onClick = {
                        val target = targetMins.toDoubleOrNull() ?: 45.0
                        onAdd(title, priority, "time_based", target, "minutes", true)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary, disabledContainerColor = SkyBlueSurface),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Create Task", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (title.isNotBlank()) PureWhite else SkyBluePrimary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (title.isNotBlank()) PureWhite else SkyBluePrimary.copy(alpha = 0.5f))
                }
            }
        }
    }
}
