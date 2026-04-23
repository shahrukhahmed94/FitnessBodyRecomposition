package com.tsapps.fitnessbodyrecomposition.di

import com.tsapps.fitnessbodyrecomposition.data.repository.FirestoreService
import com.tsapps.fitnessbodyrecomposition.ui.dashboard.DashboardViewModel
import com.tsapps.fitnessbodyrecomposition.ui.nutrition.NutritionViewModel
import com.tsapps.fitnessbodyrecomposition.ui.workout.WorkoutViewModel
import com.tsapps.fitnessbodyrecomposition.ui.steps.StepsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { FirestoreService() }
    viewModelOf(::DashboardViewModel)
    viewModelOf(::NutritionViewModel)
    viewModelOf(::WorkoutViewModel)
    viewModelOf(::StepsViewModel)
}
