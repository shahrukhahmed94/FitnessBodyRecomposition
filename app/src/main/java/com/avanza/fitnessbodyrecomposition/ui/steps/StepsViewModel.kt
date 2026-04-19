package com.avanza.fitnessbodyrecomposition.ui.steps

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepsViewModel(private val context: Context) : ViewModel(), SensorEventListener {

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val sharedPreferences = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

    init {
        // Only valid if sensor exists, but we also just load from prefs initially to prevent 0 visual jitter
        val savedSteps = sharedPreferences.getInt("current_day_steps", 0)
        _steps.value = savedSteps
    }

    fun startTracking() {
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val savedDate = sharedPreferences.getString("step_date", "")
            
            if (savedDate != currentDate) {
                // It's a new day! The sensor totalSteps keeps increasing until reboot.
                // We mark the current totalSteps as the baseline for the new day.
                sharedPreferences.edit()
                    .putString("step_date", currentDate)
                    .putInt("step_baseline", totalSteps)
                    .putInt("current_day_steps", 0)
                    .apply()
                _steps.value = 0
            } else {
                // Normal day tracking
                val baseline = sharedPreferences.getInt("step_baseline", -1)
                if (baseline == -1) {
                    // First time tracking today but we don't have a baseline yet
                    sharedPreferences.edit().putInt("step_baseline", totalSteps).apply()
                    _steps.value = 0
                } else {
                    val currentDaySteps = totalSteps - baseline
                    // In case of a reboot, totalSteps resets to 0 (or small number), so baseline > totalSteps
                    if (currentDaySteps < 0) {
                        // Reboot detected, reset baseline
                        sharedPreferences.edit().putInt("step_baseline", totalSteps).apply()
                        _steps.value = 0
                    } else {
                        sharedPreferences.edit().putInt("current_day_steps", currentDaySteps).apply()
                        _steps.value = currentDaySteps
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
