package com.personal.lifeos.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.personal.lifeos.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.CheckCircle)
    object Health : Screen("health", "Health", Icons.Default.DirectionsWalk)
    object Food : Screen("food", "Food", Icons.Default.Restaurant)
    object Goals : Screen("goals", "Goals", Icons.Default.EmojiEvents)
    object AI : Screen("ai", "Life AI", Icons.Default.AutoAwesome)
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Tasks,
    Screen.Health,
    Screen.Food,
    Screen.AI
)

@Composable
fun LifeOsBottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = PureWhite,
        contentColor = SkyBluePrimary,
        tonalElevation = 8.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        BottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) SkyBluePrimary else TextMuted
                    )
                },
                label = {
                    Text(
                        screen.title,
                        color = if (isSelected) SkyBluePrimary else TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SkyBlueSurface,
                    selectedIconColor = SkyBluePrimary,
                    unselectedIconColor = TextMuted
                )
            )
        }
    }
}
