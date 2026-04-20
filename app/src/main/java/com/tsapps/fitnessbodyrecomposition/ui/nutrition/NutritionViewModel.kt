package com.tsapps.fitnessbodyrecomposition.ui.nutrition

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NutritionViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        loadMeals()
    }

    fun refreshMeals() {
        loadMeals()
    }

    private fun loadMeals() {
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val mealsJson = sharedPref.getString("meals", "[]") ?: "[]"
        
        val meals = mutableListOf<Meal>()
        val jsonArray = JSONArray(mealsJson)
        
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            meals.add(
                Meal(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    mealType = obj.getString("mealType"),
                    calories = obj.getInt("calories"),
                    protein = obj.getInt("protein"),
                    carbs = obj.getInt("carbs"),
                    fat = obj.getInt("fat"),
                    timestamp = obj.getLong("timestamp")
                )
            )
        }
        
        _uiState.update { it.copy(allMeals = meals) }
        calculateDailyTotals()
    }

    fun addMeal(name: String, mealType: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        val newMeal = Meal(
            id = UUID.randomUUID().toString(),
            name = name,
            mealType = mealType,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            timestamp = System.currentTimeMillis()
        )
        
        val updatedMeals = _uiState.value.allMeals + newMeal
        _uiState.update { it.copy(allMeals = updatedMeals) }
        
        saveMeals(updatedMeals)
        calculateDailyTotals()
    }

    fun deleteMeal(mealId: String) {
        val updatedMeals = _uiState.value.allMeals.filter { it.id != mealId }
        _uiState.update { it.copy(allMeals = updatedMeals) }
        saveMeals(updatedMeals)
        calculateDailyTotals()
    }

    private fun saveMeals(meals: List<Meal>) {
        val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        
        meals.forEach { meal ->
            val obj = JSONObject().apply {
                put("id", meal.id)
                put("name", meal.name)
                put("mealType", meal.mealType)
                put("calories", meal.calories)
                put("protein", meal.protein)
                put("carbs", meal.carbs)
                put("fat", meal.fat)
                put("timestamp", meal.timestamp)
            }
            jsonArray.put(obj)
        }
        
        sharedPref.edit().putString("meals", jsonArray.toString()).apply()
    }

    private fun calculateDailyTotals() {
        val today = getTodayDateString()
        val todayMeals = _uiState.value.allMeals.filter { 
            getDateString(it.timestamp) == today 
        }
        
        val totalCalories = todayMeals.sumOf { it.calories }
        val totalProtein = todayMeals.sumOf { it.protein }
        val totalCarbs = todayMeals.sumOf { it.carbs }
        val totalFat = todayMeals.sumOf { it.fat }
        
        _uiState.update {
            it.copy(
                todayMeals = todayMeals,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat
            )
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

data class NutritionUiState(
    val allMeals: List<Meal> = emptyList(),
    val todayMeals: List<Meal> = emptyList(),
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0
)

data class Meal(
    val id: String,
    val name: String,
    val mealType: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val timestamp: Long
)
