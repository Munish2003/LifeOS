package com.personal.lifeos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.personal.lifeos.ui.navigation.LifeOsBottomBar
import com.personal.lifeos.ui.navigation.Screen
import com.personal.lifeos.ui.screens.ai.AiCompanionScreen
import com.personal.lifeos.ui.screens.food.FoodNutritionScreen
import com.personal.lifeos.ui.screens.goals.GoalsChallengesScreen
import com.personal.lifeos.ui.screens.health.HealthStepsScreen
import com.personal.lifeos.ui.screens.home.HomeDashboardScreen
import com.personal.lifeos.ui.screens.routine.RoutineAvailabilityScreen
import com.personal.lifeos.ui.screens.tasks.TasksScreen
import com.personal.lifeos.ui.screens.timer.FocusTimerScreen
import com.personal.lifeos.ui.theme.LifeOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeOSTheme {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            LifeOsBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeDashboardScreen(
                    onNavigateToTimer = { taskTitle ->
                        navController.navigate("timer/${taskTitle}")
                    },
                    onNavigateToSchedule = {
                        navController.navigate("routine")
                    }
                )
            }

            composable(Screen.Tasks.route) {
                TasksScreen(
                    onNavigateToTimer = { taskTitle ->
                        navController.navigate("timer/${taskTitle}")
                    }
                )
            }

            composable(Screen.Health.route) {
                HealthStepsScreen()
            }

            composable(Screen.Food.route) {
                FoodNutritionScreen()
            }

            composable(Screen.Goals.route) {
                GoalsChallengesScreen()
            }

            composable(Screen.AI.route) {
                AiCompanionScreen()
            }

            composable("routine") {
                RoutineAvailabilityScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "timer/{taskTitle}",
                arguments = listOf(navArgument("taskTitle") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskTitle = backStackEntry.arguments?.getString("taskTitle") ?: "Focus Task"
                FocusTimerScreen(
                    taskTitle = taskTitle,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
