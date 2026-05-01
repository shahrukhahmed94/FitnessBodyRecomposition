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
import androidx.compose.material.icons.filled.Add
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
import com.tsapps.fitnessbodyrecomposition.ui.components.InterstitialAdHelper
import com.tsapps.fitnessbodyrecomposition.ui.components.AdUnitIds

data class Exercise(val name: String, val sets: Int, val reps: String)

val mockExercises = mapOf(
    "home" to listOf(
        Exercise("Bodyweight Squats", 3, "15-20"),
        Exercise("Push-Ups", 3, "AMRAP"),
        Exercise("Lunges", 3, "12-15/leg"),
        Exercise("Plank", 3, "60s"),
        Exercise("Burpees", 3, "10-15")
    ),
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
    val initialExercises = mockExercises[routineId] ?: if (routineId == "custom") emptyList() else listOf(
        Exercise("Warm Up", 1, "5 mins"),
        Exercise("Main Compound Lift", 3, "5-8"),
        Exercise("Accessory Movement", 3, "8-12"),
        Exercise("Isolation Movement", 3, "12-15")
    )
    val exercises = remember { mutableStateListOf(*initialExercises.toTypedArray()) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    // Preload interstitial ad
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        InterstitialAdHelper.loadAd(context, AdUnitIds.WORKOUT_COMPLETE_INTERSTITIAL)
    }

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
                        
                        // Show interstitial ad after workout, then navigate back
                        val activity = context as? android.app.Activity
                        if (activity != null) {
                            InterstitialAdHelper.showAd(activity) {
                                navController.popBackStack()
                            }
                        } else {
                            navController.popBackStack()
                        }
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
                        val weight = enteredWeights["${exercise.name}_$setIndex"]
                        if (repsVal.isNotEmpty() && !weight.isNullOrEmpty()) {
                            completedSets["${exercise.name}_$setIndex"] = true
                        }
                    },
                    onWeightChanged = { setIndex, weightVal ->
                        enteredWeights["${exercise.name}_$setIndex"] = weightVal
                        val reps = enteredReps["${exercise.name}_$setIndex"]
                        if (weightVal.isNotEmpty() && !reps.isNullOrEmpty()) {
                            completedSets["${exercise.name}_$setIndex"] = true
                        }
                    }
                )
            }
            
            if (routineId == "custom") {
                item {
                    Button(
                        onClick = { showAddExerciseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor, contentColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Custom Exercise", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Added space for bottom bar
            }
        }
    }

    if (showAddExerciseDialog) {
        var newExerciseName by remember { mutableStateOf("") }
        var newExerciseSets by remember { mutableStateOf("3") }
        var newExerciseReps by remember { mutableStateOf("8-12") }

        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            containerColor = SurfaceColor,
            titleContentColor = TextWhite,
            textContentColor = TextGrey,
            title = { Text("Add Custom Exercise") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newExerciseName,
                        onValueChange = { newExerciseName = it },
                        label = { Text("Exercise Name", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            focusedBorderColor = NeonGreen, unfocusedBorderColor = TextGrey,
                            cursorColor = NeonGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newExerciseSets,
                        onValueChange = { newExerciseSets = it },
                        label = { Text("Target Sets", color = TextGrey) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            focusedBorderColor = NeonGreen, unfocusedBorderColor = TextGrey,
                            cursorColor = NeonGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newExerciseReps,
                        onValueChange = { newExerciseReps = it },
                        label = { Text("Target Reps (e.g. 8-12)", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            focusedBorderColor = NeonGreen, unfocusedBorderColor = TextGrey,
                            cursorColor = NeonGreen
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val setsInt = newExerciseSets.toIntOrNull() ?: 3
                    exercises.add(Exercise(newExerciseName.ifBlank { "Custom Exercise" }, setsInt, newExerciseReps))
                    showAddExerciseDialog = false
                }) {
                    Text("Add", color = NeonGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Cancel", color = TextGrey)
                }
            }
        )
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
             }
             Spacer(modifier = Modifier.height(12.dp))
             
             // Column Headers
             Row(
                 modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Text(text = "SET", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                 Text(text = "TARGET", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                 Text(text = "KG", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                 Text(text = "REPS", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
                 Spacer(modifier = Modifier.width(40.dp)) // For Checkbox alignment
             }

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
                     Text(text = "${setNum + 1}", color = TextWhite, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                     Text(text = exercise.reps, color = TextGrey, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                     
                     WorkoutValueDropdown(
                         value = weightVal,
                         onValueChange = { onWeightChanged(setNum, it) },
                         options = listOf("0", "2.5", "5", "7.5", "10", "12.5", "15", "17.5", "20", "22.5", "25", "30", "35", "40", "45", "50", "60", "70", "80", "90", "100", "120", "140", "160", "180", "200"),
                         placeholder = "kg",
                         modifier = Modifier.width(80.dp).padding(end = 4.dp)
                     )
                     
                     WorkoutValueDropdown(
                         value = repsVal,
                         onValueChange = { onRepsChanged(setNum, it) },
                         options = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "25", "30"),
                         placeholder = "reps",
                         modifier = Modifier.width(70.dp).padding(end = 4.dp)
                     )
                     
                     Icon(
                         imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                         contentDescription = "Complete",
                         tint = if (isChecked) NeonGreen else TextGrey,
                         modifier = Modifier.clickable { onSetToggled(setNum, !isChecked) }.size(32.dp).padding(start = 4.dp)
                     )
                 }
                 HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
             }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutValueDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = if (placeholder == "kg") KeyboardType.Decimal else KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = TextGrey.copy(alpha = 0.3f),
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            placeholder = { 
                Text(
                    text = placeholder, 
                    color = TextGrey.copy(alpha = 0.5f), 
                    modifier = Modifier.fillMaxWidth(), 
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                ) 
            },
            trailingIcon = null // Keep it clean for narrow columns
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceColor).heightIn(max = 250.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, color = TextWhite) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
