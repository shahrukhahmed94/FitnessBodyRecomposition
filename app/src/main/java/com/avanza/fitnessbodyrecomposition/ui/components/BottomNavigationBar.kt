package com.avanza.fitnessbodyrecomposition.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.avanza.fitnessbodyrecomposition.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Workout,
        BottomNavItem.Nutrition,
        BottomNavItem.Progress,
        BottomNavItem.Steps
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.Dashboard.route) {
                            saveState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Default.Home, Screen.Dashboard.route)
    object Workout : BottomNavItem("Workout", Icons.Default.FitnessCenter, "workout")
    object Nutrition : BottomNavItem("Nutrition", Icons.Default.Restaurant, Screen.Nutrition.route)
    object Progress : BottomNavItem("Progress", Icons.Default.Analytics, Screen.Progress.route)
    object Steps : BottomNavItem("Steps", Icons.Default.DirectionsWalk, Screen.Steps.route)
}
