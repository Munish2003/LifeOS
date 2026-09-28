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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    var isEstimating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeViewModel.repository.getTodayFoodEntries().collect {
            foodList = it
        }
    }

    val totalCalories = foodList.sumOf { it.calories }
    val targetCalories = 1200.0
    val remainingCalories = kotlin.math.max(0.0, targetCalories - totalCalories)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Food & Calorie Tracker",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp), ambientColor = GlowSkyBlue),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CALORIE BUDGET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyBluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${totalCalories.toInt()} / ${targetCalories.toInt()} kcal",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextDarkPrimary,
                                    fontSize = 32.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (remainingCalories > 0) SkyBlueSurface else AmberWarning.copy(alpha = 0.15f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "${remainingCalories.toInt()} kcal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingCalories > 0) SkyBluePrimary else AmberWarning
                                    )
                                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Macro Nutrients Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val totalProt = foodList.sumOf { it.proteinG }
                            val totalCarbs = foodList.sumOf { it.carbsG }
                            val totalFat = foodList.sumOf { it.fatG }
                            SkyMacroTag("Protein", "${totalProt.toInt()}g", SkyBluePrimary)
                            SkyMacroTag("Carbs", "${totalCarbs.toInt()}g", CyanAccent)
                            SkyMacroTag("Fats", "${totalFat.toInt()}g", AmberWarning)
                        }
                    }
                }
            }

            // Quick Natural Language or Photo Log Input
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "AI FOOD LOGGER (TEXT / VOICE / OCR)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = foodInput,
                            onValueChange = { foodInput = it },
                            placeholder = { Text("e.g. 2 roti paneer, 1 bowl dal, sandwich...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (foodInput.isNotBlank()) {
                                    isEstimating = true
                                    scope.launch {
                                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                        val estimate = homeViewModel.repository.parseFoodText(foodInput)
                                        val cal = estimate?.calories ?: 250.0
                                        val name = estimate?.food_name ?: foodInput
                                        homeViewModel.repository.addFoodEntry(
                                            FoodEntity(
                                                date = today,
                                                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                                mealType = "meal",
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
                                        isEstimating = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary, contentColor = PureWhite)
                        ) {
                            if (isEstimating) {
                                CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Estimating Nutrition...")
                            } else {
                                Icon(Icons.Default.AddCircle, contentDescription = "Log Food", tint = PureWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Estimate Calories & Log Food", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "TODAY'S LOGGED MEALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            if (foodList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No meals logged yet today. Type a meal above to track!", color = TextDarkSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(foodList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.foodName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${item.mealType.uppercase()} • ${item.time ?: "Logged"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextDarkSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.calories.toInt()} kcal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SkyBluePrimary
                                )
                                Text(
                                    text = "P:${item.proteinG.toInt()}g C:${item.carbsG.toInt()}g F:${item.fatG.toInt()}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkyMacroTag(name: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(name, style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
