package com.avanza.fitnessbodyrecomposition.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.avanza.fitnessbodyrecomposition.ui.components.BottomNavigationBar
import com.avanza.fitnessbodyrecomposition.ui.dashboard.DashboardScreen
import com.avanza.fitnessbodyrecomposition.ui.onboarding.OnboardingScreen
import com.avanza.fitnessbodyrecomposition.ui.onboarding.ProfileSetupScreen
import com.avanza.fitnessbodyrecomposition.ui.nutrition.AddMealScreen
import com.avanza.fitnessbodyrecomposition.ui.nutrition.NutritionScreen
import com.avanza.fitnessbodyrecomposition.ui.profile.ProfileScreen
import com.avanza.fitnessbodyrecomposition.ui.progress.ProgressScreen
import com.avanza.fitnessbodyrecomposition.ui.workout.ActiveWorkoutScreen
import com.avanza.fitnessbodyrecomposition.ui.workout.WorkoutScreen
import com.avanza.fitnessbodyrecomposition.ui.workout.WorkoutViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    val onboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)
    val startDestination = if (onboardingCompleted) Screen.Dashboard.route else Screen.Onboarding.route

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Shared ViewModel for Workout Flow
    val workoutViewModel: WorkoutViewModel = koinViewModel()

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Onboarding.route && 
                currentRoute != Screen.ProfileSetup.route && 
                currentRoute != Screen.Profile.route
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(navController)
            }
            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController)
            }
            
            composable(Screen.WorkoutSelection.route) {
                WorkoutScreen(navController, workoutViewModel)
            }
            
            composable(Screen.ActiveWorkout.route) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId") ?: "push"
                ActiveWorkoutScreen(navController, routineId, workoutViewModel)
            }

            // Add placeholder destinations for other tabs to prevent crashes for now
            composable("workout") { 
                 // If bottom bar clicks 'workout', go to selection for now
                 WorkoutScreen(navController, workoutViewModel) 
            }
            
            composable(Screen.Progress.route) {
                ProgressScreen(navController)
            }
            
            composable(Screen.Nutrition.route) {
                NutritionScreen(navController)
            }
            
            composable(Screen.AddMeal.route) {
                AddMealScreen(navController)
            }
            
            composable(Screen.Steps.route) {
                com.avanza.fitnessbodyrecomposition.ui.steps.StepsScreen(navController)
            }
        }
    }
}
