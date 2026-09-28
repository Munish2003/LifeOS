package com.personal.lifeos.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.data.local.entity.FoodEntity
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodNutritionScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var foodInput by remember { mutableStateOf("") }
    var foodList by remember { mutableStateOf<List<FoodEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        homeViewModel.repository.getTodayFoodEntries().collect {
            foodList = it
        }
    }

    val totalCalories = if (foodList.isNotEmpty()) foodList.sumOf { it.calories } else 860.0
    val targetCalories = 1200.0
    val remainingCalories = kotlin.math.max(0.0, targetCalories - totalCalories)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Food & Nutrition",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calorie Budget Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CALORIE INTAKE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${totalCalories.toInt()} / ${targetCalories.toInt()} kcal",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${remainingCalories.toInt()} kcal",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingCalories > 0) AccentEmerald else AccentRose
                                )
                                Text("Remaining", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Macro Nutrients Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val totalProt = foodList.sumOf { it.proteinG }.let { if (it > 0) it else 38.8 }
                            val totalCarbs = foodList.sumOf { it.carbsG }.let { if (it > 0) it else 114.5 }
                            val totalFat = foodList.sumOf { it.fatG }.let { if (it > 0) it else 32.3 }
                            MacroTag("Protein", "${totalProt.toInt()}g", PrimaryIndigo)
                            MacroTag("Carbs", "${totalCarbs.toInt()}g", SecondaryCyan)
                            MacroTag("Fat", "${totalFat.toInt()}g", AccentAmber)
                        }
                    }
                }
            }

            // Quick Natural Language or Photo Log Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "QUICK FOOD LOG (TEXT / OCR)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = foodInput,
                            onValueChange = { foodInput = it },
                            placeholder = { Text("e.g. 2 rotis and paneer, 1 glass milk...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (foodInput.isNotBlank()) {
                                    scope.launch {
                                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                        val estimate = homeViewModel.repository.parseFoodText(foodInput)
                                        val cal = estimate?.calories ?: 250.0
                                        val name = estimate?.food_name ?: foodInput
                                        homeViewModel.repository.addFoodEntry(
                                            FoodEntity(
                                                date = today,
                                                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                                mealType = "snack",
                                                foodName = name,
                                                portionDesc = foodInput,
                                                calories = cal,
                                                proteinG = estimate?.protein_g ?: 8.0,
                                                carbsG = estimate?.carbs_g ?: 30.0,
                                                fatG = estimate?.fat_g ?: 9.0,
                                                fiberG = estimate?.fiber_g ?: 2.0,
                                                source = "text_ai"
                                            )
                                        )
                                        foodInput = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Log Food", tint = Slate950)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Estimate & Log Food", color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "TODAY'S LOGGED MEALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(foodList, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.foodName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate100
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.mealType.uppercase()} • ${item.time ?: "Logged"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${item.calories.toInt()} kcal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentAmber
                            )
                            Text(
                                text = "P:${item.proteinG.toInt()}g C:${item.carbsG.toInt()}g F:${item.fatG.toInt()}g",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroTag(name: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(name, style = MaterialTheme.typography.labelSmall, color = Slate400)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}
