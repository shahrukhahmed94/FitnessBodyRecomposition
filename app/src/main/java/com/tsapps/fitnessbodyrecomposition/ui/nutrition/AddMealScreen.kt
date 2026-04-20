package com.tsapps.fitnessbodyrecomposition.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.SurfaceColor
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    navController: NavController,
    viewModel: NutritionViewModel = koinViewModel()
) {
    var mealName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var expanded by remember { mutableStateOf(false) }
    
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Meal", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val uiState by viewModel.uiState.collectAsState()
            val recentMeals = uiState.allMeals.distinctBy { it.name.lowercase() }.takeLast(5)
            
            Text("Quick Estimates", color = TextWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        mealName = "Light Snack"
                        calories = "200"; protein = "15"; carbs = "25"; fat = "5"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("Light", color = NeonGreen) }
                
                Button(
                    onClick = {
                        mealName = "Medium Meal"
                        calories = "600"; protein = "40"; carbs = "65"; fat = "20"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("Medium", color = NeonGreen) }
                
                Button(
                    onClick = {
                        mealName = "Heavy Meal"
                        calories = "1200"; protein = "60"; carbs = "120"; fat = "50"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("Heavy", color = NeonGreen) }
            }
            
            if (recentMeals.isNotEmpty()) {
                Text("Recent Meals", color = TextWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentMeals) { meal ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                mealName = meal.name
                                selectedMealType = meal.mealType
                                calories = meal.calories.toString()
                                protein = meal.protein.toString()
                                carbs = meal.carbs.toString()
                                fat = meal.fat.toString()
                            },
                            label = { Text(meal.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceColor, 
                                labelColor = TextWhite
                            )
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.DarkGray.copy(alpha = 0.5f))
            
            // Meal Name
            OutlinedTextField(
                value = mealName,
                onValueChange = { mealName = it },
                label = { Text("Meal Name", color = TextGrey) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextGrey,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = NeonGreen
                )
            )
            
            // Meal Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedMealType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Meal Type", color = TextGrey) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = TextGrey,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    mealTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedMealType = type
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Calories
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it.filter { char -> char.isDigit() } },
                label = { Text("Calories", color = TextGrey) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextGrey,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = NeonGreen
                )
            )
            
            Text(
                text = "Macronutrients",
                color = TextWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Protein
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it.filter { char -> char.isDigit() } },
                label = { Text("Protein (g)", color = TextGrey) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextGrey,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = NeonGreen
                )
            )
            
            // Carbs
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it.filter { char -> char.isDigit() } },
                label = { Text("Carbs (g)", color = TextGrey) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextGrey,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = NeonGreen
                )
            )
            
            // Fat
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it.filter { char -> char.isDigit() } },
                label = { Text("Fat (g)", color = TextGrey) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextGrey,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = NeonGreen
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (mealName.isNotBlank() && calories.isNotBlank()) {
                        viewModel.addMeal(
                            name = mealName,
                            mealType = selectedMealType,
                            calories = calories.toIntOrNull() ?: 0,
                            protein = protein.toIntOrNull() ?: 0,
                            carbs = carbs.toIntOrNull() ?: 0,
                            fat = fat.toIntOrNull() ?: 0
                        )
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = mealName.isNotBlank() && calories.isNotBlank()
            ) {
                Text("Save Meal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
