package com.tsapps.fitnessbodyrecomposition.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.SurfaceColor
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import org.koin.androidx.compose.koinViewModel
import com.tsapps.fitnessbodyrecomposition.data.model.CompletedExercise
import com.tsapps.fitnessbodyrecomposition.data.model.LoggedSet
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

data class Exercise(val name: String, val sets: Int, val reps: String)

val mockExercises = mapOf(
    "push" to listOf(
        Exercise("Bench Press", 3, "8-12"),
        Exercise("Overhead Press", 3, "8-12"),
        Exercise("Incline Dumbbell Press", 3, "10-12"),
        Exercise("Lateral Raises", 3, "12-15"),
        Exercise("Tricep Pushdowns", 3, "12-15")
    ),
    "pull" to listOf(
        Exercise("Deadlift", 3, "5-8"),
        Exercise("Pull Ups", 3, "AMRAP"),
        Exercise("Lat Pulldown", 3, "8-12"),
        Exercise("Horizontal Pulldown", 3, "8-12"),
        Exercise("T-Bar Rows", 3, "8-12"),
        Exercise("Barbell Rows", 3, "8-12"),
        Exercise("Face Pulls", 3, "12-15"),
        Exercise("Bicep Curls", 3, "10-12")
    ),
    "legs" to listOf(
        Exercise("Squats", 3, "6-10"),
        Exercise("Leg Press", 3, "10-12"),
        Exercise("Romanian Deadlifts", 3, "8-12"),
        Exercise("Leg Extensions", 3, "12-15"),
        Exercise("Calf Raises", 4, "15-20")
    ),
    "forearms" to listOf(
        Exercise("Dumbbell Wrist Curls", 3, "12-15"),
        Exercise("Reverse Wrist Curls", 3, "12-15"),
        Exercise("Hammer Curls", 3, "10-12"),
        Exercise("Farmer's Walk", 3, "30-45s")
    )
    // Add others if needed, fallback to generic
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    navController: NavController, 
    routineId: String,
    viewModel: WorkoutViewModel = koinViewModel()
) {
    val routineName = mockRoutines.find { it.id == routineId }?.name ?: "Workout"
    val exercises = mockExercises[routineId] ?: listOf(
        Exercise("Warm Up", 1, "5 mins"),
        Exercise("Main Compound Lift", 3, "5-8"),
        Exercise("Accessory Movement", 3, "8-12"),
        Exercise("Isolation Movement", 3, "12-15")
    )

    // State map to track checkbox toggles: Key is "ExerciseName_SetNumber"
    val completedSets = remember { mutableStateMapOf<String, Boolean>() }
    val enteredReps = remember { mutableStateMapOf<String, String>() }
    val enteredWeights = remember { mutableStateMapOf<String, String>() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routineName, color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                     IconButton(onClick = { /* Timer logic TODO */ }) {
                         Icon(Icons.Default.Timer, contentDescription = "Timer", tint = NeonGreen)
                     }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { 
                        // Map checked state down to CompletedExercise objects
                        val completedList = exercises.map { ex ->
                            val loggedDetails = mutableListOf<LoggedSet>()
                            for (i in 0 until ex.sets) {
                                if (completedSets["${ex.name}_${i}"] == true) {
                                    val repsInput = enteredReps["${ex.name}_${i}"]
                                    val weightInput = enteredWeights["${ex.name}_${i}"]
                                    val repsInt = repsInput?.toIntOrNull() ?: 0
                                    val weightFloat = weightInput?.toFloatOrNull() ?: 0f
                                    loggedDetails.add(LoggedSet(setIndex = i, reps = repsInt, weight = weightFloat))
                                }
                            }
                            CompletedExercise(
                                name = ex.name,
                                targetSets = ex.sets,
                                loggedSets = loggedDetails
                            )
                        }
                    
                        viewModel.logWorkout(routineId, routineName, completedList)
                        navController.popBackStack() 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Finish Workout", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Exercises",
                    color = TextGrey,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    completedSets = completedSets,
                    enteredReps = enteredReps,
                    enteredWeights = enteredWeights,
                    onSetToggled = { setIndex, isChecked ->
                        completedSets["${exercise.name}_$setIndex"] = isChecked
                    },
                    onRepsChanged = { setIndex, repsVal ->
                        enteredReps["${exercise.name}_$setIndex"] = repsVal
                    },
                    onWeightChanged = { setIndex, weightVal ->
                        enteredWeights["${exercise.name}_$setIndex"] = weightVal
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    completedSets: Map<String, Boolean>,
    enteredReps: Map<String, String>,
    enteredWeights: Map<String, String>,
    onSetToggled: (Int, Boolean) -> Unit,
    onRepsChanged: (Int, String) -> Unit,
    onWeightChanged: (Int, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
             Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                 Text(text = exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                 // Placeholder for menu or info
             }
             Spacer(modifier = Modifier.height(8.dp))
             
             // Render sets rows
             repeat(exercise.sets) { setNum ->
                 val isChecked = completedSets["${exercise.name}_$setNum"] == true
                 val repsVal = enteredReps["${exercise.name}_$setNum"] ?: ""
                 val weightVal = enteredWeights["${exercise.name}_$setNum"] ?: ""
                 
                 Row(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(vertical = 4.dp),
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Text(text = "${setNum + 1}", color = TextGrey, modifier = Modifier.width(20.dp))
                     Text(text = exercise.reps, color = TextGrey, modifier = Modifier.weight(1f))
                     
                     OutlinedTextField(
                         value = weightVal,
                         onValueChange = { onWeightChanged(setNum, it) },
                         modifier = Modifier.width(70.dp).padding(end = 8.dp),
                         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                         colors = OutlinedTextFieldDefaults.colors(
                             focusedBorderColor = NeonGreen,
                             unfocusedBorderColor = TextGrey,
                             focusedTextColor = TextWhite,
                             unfocusedTextColor = TextWhite
                         ),
                         singleLine = true,
                         textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                         placeholder = { Text("kg", color = TextGrey, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                     )
                     
                     OutlinedTextField(
                         value = repsVal,
                         onValueChange = { onRepsChanged(setNum, it) },
                         modifier = Modifier.width(60.dp).padding(end = 8.dp),
                         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                         colors = OutlinedTextFieldDefaults.colors(
                             focusedBorderColor = NeonGreen,
                             unfocusedBorderColor = TextGrey,
                             focusedTextColor = TextWhite,
                             unfocusedTextColor = TextWhite
                         ),
                         singleLine = true,
                         textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                         placeholder = { Text("reps", color = TextGrey, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                     )
                     
                     Icon(
                         imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                         contentDescription = "Complete",
                         tint = if (isChecked) NeonGreen else TextGrey,
                         modifier = Modifier.clickable { onSetToggled(setNum, !isChecked) }.size(32.dp)
                     )
                 }
                 HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
             }
        }
    }
}
