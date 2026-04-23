package com.tsapps.fitnessbodyrecomposition.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tsapps.fitnessbodyrecomposition.ui.components.LineGraph
import com.tsapps.fitnessbodyrecomposition.ui.dashboard.DashboardViewModel
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.SurfaceColor
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.tsapps.fitnessbodyrecomposition.data.model.WeightLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Weight")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Your Progress", color = TextWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
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
            // Weight Trend Graph
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weight Trend",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.weightHistory.isNotEmpty()) {
                            LineGraph(
                                dataPoints = uiState.weightHistory,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No data available", color = TextGrey)
                            }
                        }
                    }
                }
            }
            // Progressive Overload Graph
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progressive Overload",
                                color = TextWhite,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Box {
                                TextButton(onClick = { expanded = true }) {
                                    Text(
                                        text = uiState.selectedExercise.ifEmpty { "Select Exercise" },
                                        color = NeonGreen
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(SurfaceColor)
                                ) {
                                    uiState.uniqueExercises.forEach { exercise ->
                                        DropdownMenuItem(
                                            text = { Text(exercise, color = TextWhite) },
                                            onClick = {
                                                viewModel.selectExercise(exercise)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.uniqueExercises.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(uiState.uniqueExercises) { exercise ->
                                    val isSelected = exercise == uiState.selectedExercise
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectExercise(exercise) },
                                        label = { 
                                            Text(
                                                text = exercise,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ) 
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color.Transparent,
                                            labelColor = TextGrey,
                                            selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = NeonGreen
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = TextGrey.copy(alpha = 0.3f),
                                            selectedBorderColor = NeonGreen,
                                            borderWidth = 1.dp,
                                            selectedBorderWidth = 1.dp
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (uiState.exerciseWeightHistory.isNotEmpty()) {
                            LineGraph(
                                dataPoints = uiState.exerciseWeightHistory,
                                modifier = Modifier.fillMaxSize(),
                                lineColor = NeonGreen
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (uiState.selectedExercise.isEmpty()) 
                                        "No exercises logged yet" 
                                    else "Not enough data for ${uiState.selectedExercise}", 
                                    color = TextGrey,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            // Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Current",
                        value = "${uiState.currentWeight} kg",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Goal",
                        value = "${uiState.targetWeight} kg",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Recent History
            item {
                Text(
                    text = "Recent History",
                    color = TextGrey,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (uiState.weightLogs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = "No history available",
                            color = TextGrey,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            } else {
                items(uiState.weightLogs.size) { index ->
                    HistoryItem(uiState.weightLogs[index])
                }
            }
        }
    }

    if (showAddDialog) {
        var inputWeight by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Weight") },
            text = {
                OutlinedTextField(
                    value = inputWeight,
                    onValueChange = { inputWeight = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Weight (kg)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val weight = inputWeight.toDoubleOrNull()
                        if (weight != null) {
                            viewModel.logWeight(weight)
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextGrey, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = NeonGreen, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistoryItem(log: WeightLog) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Weigh In", color = TextWhite, fontWeight = FontWeight.Bold)
                val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                Text(text = formatter.format(Date(log.date)), color = TextGrey, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "${String.format("%.1f", log.weight)} kg", color = NeonGreen, fontWeight = FontWeight.Bold)
        }
    }
}
