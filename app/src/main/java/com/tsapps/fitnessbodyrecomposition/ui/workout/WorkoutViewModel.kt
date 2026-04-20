package com.tsapps.fitnessbodyrecomposition.ui.workout

import android.content.Context
import androidx.lifecycle.ViewModel
import com.tsapps.fitnessbodyrecomposition.data.model.CompletedExercise
import com.tsapps.fitnessbodyrecomposition.data.model.LoggedSet
import com.tsapps.fitnessbodyrecomposition.data.model.WorkoutLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

data class WorkoutUiState(
    val workoutLogs: List<WorkoutLog> = emptyList()
)

class WorkoutViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val savedLogsJson = sharedPref.getString("workout_logs", "[]") ?: "[]"
        val logsArray = JSONArray(savedLogsJson)
        val logs = mutableListOf<WorkoutLog>()
        
        for (i in 0 until logsArray.length()) {
            val item = logsArray.getJSONObject(i)
            
            val exercisesList = mutableListOf<CompletedExercise>()
            if (item.has("completedExercises")) {
                val exercisesArray = item.getJSONArray("completedExercises")
                for (j in 0 until exercisesArray.length()) {
                    val exItem = exercisesArray.getJSONObject(j)
                    val name = exItem.getString("name")
                    val targetSets = exItem.getInt("targetSets")
                    val loggedSetsList = mutableListOf<LoggedSet>()

                    if (exItem.has("loggedSets")) {
                        val setsArray = exItem.getJSONArray("loggedSets")
                        for (k in 0 until setsArray.length()) {
                            val setObj = setsArray.getJSONObject(k)
                            loggedSetsList.add(
                                LoggedSet(
                                    setIndex = setObj.getInt("setIndex"),
                                    reps = setObj.getInt("reps")
                                )
                            )
                        }
                    } else {
                        // Migration logic for old schema
                        val setsCompleted = exItem.optInt("setsCompleted", 0)
                        val repsString = exItem.optString("reps", "0").filter { it.isDigit() }
                        val repsCount = if (repsString.isNotEmpty()) repsString.toInt() else 0
                        for (k in 0 until setsCompleted) {
                            loggedSetsList.add(LoggedSet(setIndex = k, reps = repsCount))
                        }
                    }

                    exercisesList.add(
                        CompletedExercise(
                            name = name,
                            targetSets = targetSets,
                            loggedSets = loggedSetsList
                        )
                    )
                }
            }
            
            logs.add(
                WorkoutLog(
                    date = item.getLong("date"),
                    routineId = item.getString("routineId"),
                    routineName = item.getString("routineName"),
                    completedExercises = exercisesList
                )
            )
        }
        
        _uiState.update { it.copy(workoutLogs = logs) }
    }

    fun logWorkout(routineId: String, routineName: String, completedExercises: List<CompletedExercise> = emptyList()) {
        val newLog = WorkoutLog(
            date = System.currentTimeMillis(),
            routineId = routineId,
            routineName = routineName,
            completedExercises = completedExercises
        )
        
        val updatedLogs = listOf(newLog) + _uiState.value.workoutLogs
        saveLogsToPrefs(updatedLogs)
        
        _uiState.update { it.copy(workoutLogs = updatedLogs) }
    }

    private fun saveLogsToPrefs(logs: List<WorkoutLog>) {
        val jsonArray = JSONArray()
        logs.forEach { log ->
            val jsonObj = JSONObject()
            jsonObj.put("date", log.date)
            jsonObj.put("routineId", log.routineId)
            jsonObj.put("routineName", log.routineName)
            
            val exercisesArray = JSONArray()
            log.completedExercises.forEach { ex ->
                val exObj = JSONObject()
                exObj.put("name", ex.name)
                exObj.put("targetSets", ex.targetSets)
                
                val setsArray = JSONArray()
                ex.loggedSets.forEach { setLog ->
                    val setObj = JSONObject()
                    setObj.put("setIndex", setLog.setIndex)
                    setObj.put("reps", setLog.reps)
                    setsArray.put(setObj)
                }
                exObj.put("loggedSets", setsArray)
                exercisesArray.put(exObj)
            }
            jsonObj.put("completedExercises", exercisesArray)
            
            jsonArray.put(jsonObj)
        }
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("workout_logs", jsonArray.toString()).apply()
    }
}
