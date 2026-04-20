package com.tsapps.fitnessbodyrecomposition.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tsapps.fitnessbodyrecomposition.navigation.Screen
import com.tsapps.fitnessbodyrecomposition.ui.components.LineGraph
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.SurfaceColor
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header: Greeting + Profile Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = uiState.greeting,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextGrey
                )
                Text(
                    text = uiState.userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { navController.navigate("profile") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.userName.firstOrNull()?.toString() ?: "U",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }

        // Main Dashboard: Calories Circular Progress
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgress(
                progress = 0.75f,
                modifier = Modifier.size(200.dp),
                color = NeonGreen,
                strokeWidth = 15.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%,d", uiState.caloriesLeft),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Calories Left",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey
                )
            }
        }

        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate(Screen.WorkoutSelection.route) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Start Workout", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { navController.navigate(Screen.AddMeal.route) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Log Meal", fontWeight = FontWeight.Bold)
            }
        }

        // Macros Breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MacroCard(title = "Carbs Left", value = "${uiState.carbs}g", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MacroCard(title = "Protein Left", value = "${uiState.protein}g", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MacroCard(title = "Fat Left", value = "${uiState.fat}g", modifier = Modifier.weight(1f))
        }

        // Body Weight Trend (Placeholder for Graph)
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Body Weight Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (uiState.weightHistory.isNotEmpty()) {
                    LineGraph(
                        dataPoints = uiState.weightHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available", color = TextGrey)
                    }
                }
            }
        }
    }
}

@Composable
fun MacroCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextGrey, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = NeonGreen, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp
) {
    Canvas(modifier = modifier) {
        val sweepAngle = 360 * progress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}
