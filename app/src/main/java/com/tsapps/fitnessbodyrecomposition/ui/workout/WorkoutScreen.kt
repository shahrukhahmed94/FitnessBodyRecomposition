package com.tsapps.fitnessbodyrecomposition.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tsapps.fitnessbodyrecomposition.navigation.Screen
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.SurfaceColor
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.tsapps.fitnessbodyrecomposition.data.model.WorkoutLog

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add

data class WorkoutRoutine(val id: String, val name: String, val duration: String, val icon: ImageVector)

val mockRoutines = listOf(
    WorkoutRoutine("home", "Home Workout", "30-45 min", Icons.Default.Home),
    WorkoutRoutine("push", "Push Day", "45-60 min", Icons.Default.FitnessCenter),
    WorkoutRoutine("pull", "Pull Day", "45-60 min", Icons.Default.FitnessCenter),
    WorkoutRoutine("legs", "Legs Day", "50-65 min", Icons.Default.FitnessCenter),
    WorkoutRoutine("upper", "Upper Body", "40-50 min", Icons.Default.FitnessCenter),
    WorkoutRoutine("lower", "Lower Body", "40-50 min", Icons.Default.FitnessCenter),
    WorkoutRoutine("cardio", "Cardio & Core", "30-45 min", Icons.Default.FitnessCenter),
    WorkoutRoutine("forearms", "Forearms Focus", "15-20 min", Icons.Default.FitnessCenter),
    WorkoutRoutine("custom", "Custom Workout", "Flexible", Icons.Default.Add)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLog by remember { mutableStateOf<WorkoutLog?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Workout", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Choose a routine to start",
                    color = TextGrey,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                // To keep the grid layout within a LazyColumn we constrain the height
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(400.dp) // Adjust based on mock counts or calculate dynamically
                ) {
                    items(mockRoutines) { routine ->
                        WorkoutCard(routine = routine) {
                            navController.navigate(Screen.ActiveWorkout.createRoute(routine.id))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Recent History",
                    color = TextGrey,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (uiState.workoutLogs.isEmpty()) {
                item {
                    Text(
                        text = "No workouts logged yet.",
                        color = TextGrey,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                lazyItems(uiState.workoutLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedLog = log }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.routineName,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                                Text(
                                    text = formatter.format(Date(log.date)),
                                    color = TextGrey,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonGreen)
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View Details", tint = TextGrey)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedLog?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedLog = null },
            containerColor = SurfaceColor,
            titleContentColor = TextWhite,
            textContentColor = TextGrey,
            title = {
                val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                Text(
                    text = "${log.routineName} (${formatter.format(Date(log.date))})",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn {
                    if (log.completedExercises.isEmpty()) {
                        item {
                            Text("No exercise details recorded.", color = TextGrey)
                        }
                    } else {
                        lazyItems(log.completedExercises) { ex ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = ex.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ex.loggedSets.size} / ${ex.targetSets} sets completed",
                                    color = TextGrey,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (ex.loggedSets.isNotEmpty()) {
                                    val repsDetails = ex.loggedSets.joinToString(", ") { "Set ${it.setIndex + 1}: ${it.reps} reps @ ${it.weight} kg" }
                                    Text(
                                        text = repsDetails,
                                        color = TextGrey.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color.DarkGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedLog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun WorkoutCard(routine: WorkoutRoutine, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = routine.icon,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = routine.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = routine.duration,
                style = MaterialTheme.typography.bodySmall,
                color = TextGrey
            )
        }
    }
}
