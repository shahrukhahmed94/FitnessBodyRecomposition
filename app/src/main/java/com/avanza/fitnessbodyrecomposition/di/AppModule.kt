package com.avanza.fitnessbodyrecomposition.di

import com.avanza.fitnessbodyrecomposition.ui.dashboard.DashboardViewModel
import com.avanza.fitnessbodyrecomposition.ui.nutrition.NutritionViewModel
import com.avanza.fitnessbodyrecomposition.ui.workout.WorkoutViewModel
import com.avanza.fitnessbodyrecomposition.ui.steps.StepsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::NutritionViewModel)
    viewModelOf(::WorkoutViewModel)
    viewModelOf(::StepsViewModel)
}
