package com.tsapps.fitnessbodyrecomposition.data.model

data class LoggedSet(
    val setIndex: Int,
    val reps: Int,
    val weight: Float = 0f
)

data class CompletedExercise(
    val name: String,
    val targetSets: Int,
    val loggedSets: List<LoggedSet>
)

data class WorkoutLog(
    val date: Long,
    val routineId: String,
    val routineName: String,
    val completedExercises: List<CompletedExercise> = emptyList()
)
