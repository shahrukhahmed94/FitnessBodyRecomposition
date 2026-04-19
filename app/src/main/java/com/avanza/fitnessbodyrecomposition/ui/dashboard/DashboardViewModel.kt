package com.avanza.fitnessbodyrecomposition.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.avanza.fitnessbodyrecomposition.data.model.WeightLog

class DashboardViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        loadUserData()
        updateGreeting()
    }

    private fun loadUserData() {
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "User") ?: "User"
        val weight = sharedPref.getString("weight", "") ?: ""
        val height = sharedPref.getString("height", "") ?: ""
        val age = sharedPref.getString("age", "") ?: ""
        val targetWeight = sharedPref.getString("target_weight", "") ?: ""

        val savedLogsJson = sharedPref.getString("weight_logs", "[]") ?: "[]"
        val logsArray = JSONArray(savedLogsJson)
        val logs = mutableListOf<WeightLog>()
        for (i in 0 until logsArray.length()) {
            val item = logsArray.getJSONObject(i)
            logs.add(
                WeightLog(
                    date = item.getLong("date"),
                    weight = item.getDouble("weight")
                )
            )
        }

        _uiState.update { 
            it.copy(
                userName = name,
                weight = weight,
                height = height,
                age = age,
                targetWeight = targetWeight,
                weightLogs = logs,
                weightHistory = logs.map { log -> log.weight }.reversed() // reverse for chronological order
            ) 
        }
        calculateRecomposition()
    }

    fun saveUserData(name: String, age: String, height: String, weight: String, targetWeight: String) {
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val weightDouble = weight.toDoubleOrNull()
        
        // If they updated weight string and it has logs, optionally we could log it.
        // Let's just log it if there's no logs at all so it seeds the history.
        var logs = _uiState.value.weightLogs
        if (logs.isEmpty() && weightDouble != null) {
            val newLog = WeightLog(System.currentTimeMillis(), weightDouble)
            logs = listOf(newLog)
            saveLogsToPrefs(logs)
        }

        with(sharedPref.edit()) {
            putString("user_name", name)
            putString("age", age)
            putString("height", height)
            putString("weight", weight)
            putString("target_weight", targetWeight)
            apply()
        }
        _uiState.update { 
            it.copy(
                userName = name,
                age = age,
                height = height,
                weight = weight,
                targetWeight = targetWeight,
                weightLogs = logs,
                weightHistory = logs.map { log -> log.weight }.reversed()
            ) 
        }
        calculateRecomposition()
    }

    fun logWeight(weight: Double) {
        val newLog = WeightLog(System.currentTimeMillis(), weight)
        val updatedLogs = listOf(newLog) + _uiState.value.weightLogs
        
        saveLogsToPrefs(updatedLogs)
        
        // Let's also update the main "current log" profile weight for consistency.
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("weight", weight.toString()).apply()

        _uiState.update {
            it.copy(
                weightLogs = updatedLogs,
                weightHistory = updatedLogs.map { log -> log.weight }.reversed(),
                weight = weight.toString()
            )
        }
        calculateRecomposition()
    }

    private fun saveLogsToPrefs(logs: List<WeightLog>) {
        val jsonArray = JSONArray()
        logs.forEach { log ->
            val jsonObj = JSONObject()
            jsonObj.put("date", log.date)
            jsonObj.put("weight", log.weight)
            jsonArray.put(jsonObj)
        }
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("weight_logs", jsonArray.toString()).apply()
    }

    private fun updateGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        _uiState.update { it.copy(greeting = greeting) }
    }

    private fun calculateRecomposition() {
        val weight = _uiState.value.weight.toDoubleOrNull()
        val height = _uiState.value.height.toDoubleOrNull()
        val age = _uiState.value.age.toIntOrNull()

        if (weight != null && height != null && height > 0 && age != null) {
            val hM = height / 100
            val bmi = weight / (hM * hM)
            
            // Mifflin-St Jeor Equation (assuming Male for now as simple default, can add gender later)
            // BMR = 10W + 6.25H - 5A + 5 (Male)
            val bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5
            
            // TDEE (Sedentary default 1.2)
            val tdee = bmr * 1.2
            
            // Goal: Body Recomposition (Slight deficit or maintenance depending on BMI)
            val targetCalories = if (bmi > 25) tdee - 300 else tdee
            
            // Macros (Split: 40% Carb, 30% Protein, 30% Fat)
            val carbs = (targetCalories * 0.4 / 4).toInt()
            val protein = (targetCalories * 0.3 / 4).toInt()
            val fat = (targetCalories * 0.3 / 9).toInt()

            // Calculate Eaten Macros for Today
            var eatenCalories = 0
            var eatenCarbs = 0
            var eatenProtein = 0
            var eatenFat = 0
            
            val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
            val mealsJson = sharedPref.getString("meals", "[]") ?: "[]"
            val jsonArray = JSONArray(mealsJson)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val ts = obj.getLong("timestamp")
                if (sdf.format(Date(ts)) == todayStr) {
                    eatenCalories += obj.getInt("calories")
                    eatenCarbs += obj.getInt("carbs")
                    eatenProtein += obj.getInt("protein")
                    eatenFat += obj.getInt("fat")
                }
            }

            val remainingCalories = targetCalories.toInt() - eatenCalories
            val remainingCarbs = carbs - eatenCarbs
            val remainingProtein = protein - eatenProtein
            val remainingFat = fat - eatenFat

            val status = when {
                bmi < 18.5 -> "Underweight - Focus on Surplus"
                bmi < 24.9 -> "Healthy - Focus on Recomp"
                bmi < 29.9 -> "Overweight - Focus on Deficit"
                else -> "Obese - Focus on Deficit"
            }
            
            _uiState.update { 
                it.copy(
                    bmi = bmi, 
                    status = status,
                    caloriesLeft = remainingCalories.coerceAtLeast(0),
                    carbs = remainingCarbs.coerceAtLeast(0),
                    protein = remainingProtein.coerceAtLeast(0),
                    fat = remainingFat.coerceAtLeast(0),
                    currentWeight = weight
                ) 
            }
        } else {
             // Fallback/Default
             if (weight != null) {
                 _uiState.update { it.copy(currentWeight = weight) }
             }
        }
    }
}

data class DashboardUiState(
    val userName: String = "User",
    val greeting: String = "Good Morning,",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val targetWeight: String = "",
    val bmi: Double? = null,
    val status: String = "",
    val caloriesLeft: Int = 2000,
    val carbs: Int = 200,
    val protein: Int = 150,
    val fat: Int = 65,
    val currentWeight: Double = 0.0,
    val weightHistory: List<Double> = emptyList(),
    val weightLogs: List<WeightLog> = emptyList()
)
