package com.avanza.fitnessbodyrecomposition.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.avanza.fitnessbodyrecomposition.ui.dashboard.DashboardViewModel
import com.avanza.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.avanza.fitnessbodyrecomposition.ui.theme.TextGrey
import com.avanza.fitnessbodyrecomposition.ui.theme.TextWhite
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf(uiState.userName) }
    var age by remember { mutableStateOf(uiState.age) }
    var weight by remember { mutableStateOf(uiState.weight) }
    var targetWeight by remember { mutableStateOf(uiState.targetWeight) }
    
    // Height Logic - Initialize from saved CM value
    var isCm by remember { mutableStateOf(true) }
    var heightCm by remember { mutableStateOf(uiState.height) }
    var heightFeet by remember { mutableStateOf("") }
    var heightInches by remember { mutableStateOf("") }

    // Update local state when ViewModel state changes
    LaunchedEffect(uiState) {
        if (name != uiState.userName) name = uiState.userName
        if (age != uiState.age) age = uiState.age
        if (weight != uiState.weight) weight = uiState.weight
        if (targetWeight != uiState.targetWeight) targetWeight = uiState.targetWeight
        
        // Only update heightCm if it's different and we are in CM mode or it hasn't been set
        if (heightCm != uiState.height) {
            heightCm = uiState.height
            // If we wanted to correspond ft/in, we could calculate it here, but keeping it simple for now
            // just loading into CM field by default as that's what we store.
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = TextWhite) },
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
            ProfileTextField(value = name, onValueChange = { name = it }, label = "Name")
            ProfileTextField(value = age, onValueChange = { age = it }, label = "Age", keyboardType = KeyboardType.Number)
            
            // Height Logic
            Row(verticalAlignment = Alignment.CenterVertically) {
                 Text("Height Unit:", color = TextWhite)
                 Spacer(modifier = Modifier.width(8.dp))
                 TextButton(onClick = { isCm = true }) {
                     Text("CM", color = if (isCm) NeonGreen else TextGrey, fontWeight = if(isCm) FontWeight.Bold else FontWeight.Normal)
                 }
                 TextButton(onClick = { isCm = false }) {
                     Text("Feet/Inch", color = if (!isCm) NeonGreen else TextGrey, fontWeight = if(!isCm) FontWeight.Bold else FontWeight.Normal)
                 }
            }
            
            if (isCm) {
                 ProfileTextField(value = heightCm, onValueChange = { heightCm = it }, label = "Height (cm)", keyboardType = KeyboardType.Number)
            } else {
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     ProfileTextField(value = heightFeet, onValueChange = { heightFeet = it }, label = "Feet", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                     ProfileTextField(value = heightInches, onValueChange = { heightInches = it }, label = "Inches", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                 }
            }

            ProfileTextField(value = weight, onValueChange = { weight = it }, label = "Current Weight (kg)", keyboardType = KeyboardType.Decimal)
            ProfileTextField(value = targetWeight, onValueChange = { targetWeight = it }, label = "Target Weight (kg)", keyboardType = KeyboardType.Decimal)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    var finalHeight = ""
                    if (isCm) {
                        finalHeight = heightCm
                    } else {
                        val ft = heightFeet.toDoubleOrNull() ?: 0.0
                        val inch = heightInches.toDoubleOrNull() ?: 0.0
                        val totalCm = (ft * 30.48) + (inch * 2.54)
                        finalHeight = totalCm.toInt().toString()
                    }

                    viewModel.saveUserData(name, age, finalHeight, weight, targetWeight)
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            
            if (uiState.bmi != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)),
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calculated Stats", color = NeonGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("BMI: ${String.format("%.1f", uiState.bmi)}", color = TextWhite)
                        Text("Status: ${uiState.status}", color = TextGrey)
                        Text("Recommended Calories: ${uiState.caloriesLeft}", color = TextWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonGreen,
            unfocusedBorderColor = TextGrey,
            focusedLabelColor = NeonGreen,
            unfocusedLabelColor = TextGrey,
            cursorColor = NeonGreen,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
}
