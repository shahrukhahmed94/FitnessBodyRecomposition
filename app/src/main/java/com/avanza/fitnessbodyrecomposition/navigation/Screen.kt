package com.avanza.fitnessbodyrecomposition.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object ProfileSetup : Screen("profile_setup")
    object Profile : Screen("profile")
    object Dashboard : Screen("dashboard")
    object WorkoutSelection : Screen("workout")
    object ActiveWorkout : Screen("active_workout/{routineId}") {
        fun createRoute(routineId: String) = "active_workout/$routineId"
    }
    object Progress : Screen("progress")
    object Nutrition : Screen("nutrition")
    object AddMeal : Screen("add_meal")
}
