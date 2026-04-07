package com.avanza.fitnessbodyrecomposition.data.model

data class CompletedExercise(
    val name: String,
    val setsCompleted: Int,
    val targetSets: Int,
    val reps: String
)

data class WorkoutLog(
    val date: Long,
    val routineId: String,
    val routineName: String,
    val completedExercises: List<CompletedExercise> = emptyList()
)
