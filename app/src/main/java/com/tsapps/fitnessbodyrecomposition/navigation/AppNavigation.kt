package com.tsapps.fitnessbodyrecomposition.navigation

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
import com.tsapps.fitnessbodyrecomposition.ui.components.BottomNavigationBar
import com.tsapps.fitnessbodyrecomposition.ui.dashboard.DashboardScreen
import com.tsapps.fitnessbodyrecomposition.ui.onboarding.OnboardingScreen
import com.tsapps.fitnessbodyrecomposition.ui.onboarding.ProfileSetupScreen
import com.tsapps.fitnessbodyrecomposition.ui.nutrition.AddMealScreen
import com.tsapps.fitnessbodyrecomposition.ui.nutrition.NutritionScreen
import com.tsapps.fitnessbodyrecomposition.ui.profile.ProfileScreen
import com.tsapps.fitnessbodyrecomposition.ui.progress.ProgressScreen
import com.tsapps.fitnessbodyrecomposition.ui.workout.ActiveWorkoutScreen
import com.tsapps.fitnessbodyrecomposition.ui.workout.WorkoutScreen
import com.tsapps.fitnessbodyrecomposition.ui.workout.WorkoutViewModel
import com.tsapps.fitnessbodyrecomposition.ui.auth.WelcomeScreen
import com.tsapps.fitnessbodyrecomposition.ui.auth.LoginScreen
import com.tsapps.fitnessbodyrecomposition.ui.auth.SignUpScreen
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    val onboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)
    val isGuest = sharedPref.getBoolean("is_guest", false)
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    val startDestination = when {
        !onboardingCompleted -> Screen.Onboarding.route
        isLoggedIn || isGuest -> Screen.Dashboard.route
        else -> Screen.Welcome.route
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Shared ViewModel for Workout Flow
    val workoutViewModel: WorkoutViewModel = koinViewModel()

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Onboarding.route && 
                currentRoute != Screen.Welcome.route && 
                currentRoute != Screen.Login.route && 
                currentRoute != Screen.SignUp.route && 
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
            composable(Screen.Welcome.route) {
                WelcomeScreen(navController)
            }
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(navController)
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
                com.tsapps.fitnessbodyrecomposition.ui.steps.StepsScreen(navController)
            }
        }
    }
}
