package com.tsapps.fitnessbodyrecomposition.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tsapps.fitnessbodyrecomposition.navigation.Screen
import com.tsapps.fitnessbodyrecomposition.ui.dashboard.DashboardViewModel
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.SurfaceColor
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    navController: NavController,
    nutritionViewModel: NutritionViewModel = koinViewModel(),
    dashboardViewModel: DashboardViewModel = koinViewModel()
) {
    val nutritionState by nutritionViewModel.uiState.collectAsState()
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    // Refresh meals when screen appears
    LaunchedEffect(Unit) {
        nutritionViewModel.refreshMeals()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutrition Tracker", color = TextWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddMeal.route) },
                containerColor = NeonGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal")
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
            // Daily Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Today's Summary",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Calories Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${nutritionState.totalCalories} / ${dashboardState.caloriesLeft}",
                                    color = NeonGreen,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "Calories", color = TextGrey, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            val progress = if (dashboardState.caloriesLeft > 0) {
                                (nutritionState.totalCalories.toFloat() / dashboardState.caloriesLeft.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                            
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(60.dp),
                                color = NeonGreen,
                                strokeWidth = 6.dp,
                                trackColor = Color.DarkGray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Macros Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroItem("Protein", nutritionState.totalProtein, dashboardState.protein, "g")
                            MacroItem("Carbs", nutritionState.totalCarbs, dashboardState.carbs, "g")
                            MacroItem("Fat", nutritionState.totalFat, dashboardState.fat, "g")
                        }
                    }
                }
            }
            
            // Recommended Diet Plan
            item {
                Text(
                    text = "Recommended Diet Plan",
                    color = TextGrey,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(nutritionState.recommendedDietPlan) { planItem ->
                        DietPlanCard(planItem)
                    }
                }
            }
            
            // Meals List Header
            item {
                Text(
                    text = "Today's Meals",
                    color = TextGrey,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Meals List
            if (nutritionState.todayMeals.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No meals logged yet", color = TextGrey)
                        }
                    }
                }
            } else {
                items(nutritionState.todayMeals) { meal ->
                    MealCard(meal, onDelete = { nutritionViewModel.deleteMeal(meal.id) })
                }
            }
            // Banner Ad
            item {
                com.tsapps.fitnessbodyrecomposition.ui.components.BannerAd(
                    adUnitId = com.tsapps.fitnessbodyrecomposition.ui.components.AdUnitIds.NUTRITION_BANNER
                )
            }
            
            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun MacroItem(label: String, current: Int, target: Int, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$current / $target$unit",
            color = TextWhite,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, color = TextGrey, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MealCard(meal: Meal, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = meal.name, color = TextWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = meal.mealType,
                        color = NeonGreen,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${meal.calories} cal • P: ${meal.protein}g • C: ${meal.carbs}g • F: ${meal.fat}g",
                    color = TextGrey,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatTime(meal.timestamp),
                    color = TextGrey.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextGrey)
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun DietPlanCard(item: DietPlanItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.name,
                color = NeonGreen,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                color = TextWhite,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DietMetric("P", "${item.protein}g")
                DietMetric("C", "${item.carbs}g")
                DietMetric("F", "${item.fat}g")
                DietMetric("Cal", "${item.calories}")
            }
        }
    }
}

@Composable
fun DietMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextGrey, style = MaterialTheme.typography.labelSmall)
        Text(text = value, color = TextWhite, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
