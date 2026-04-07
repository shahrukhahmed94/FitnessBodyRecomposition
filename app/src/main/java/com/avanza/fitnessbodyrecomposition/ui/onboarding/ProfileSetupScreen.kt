package com.avanza.fitnessbodyrecomposition.ui.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.avanza.fitnessbodyrecomposition.navigation.Screen
import com.avanza.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.avanza.fitnessbodyrecomposition.ui.theme.TextGrey
import com.avanza.fitnessbodyrecomposition.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavController) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var currentWeight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    
    // Height Inputs
    var isCm by remember { mutableStateOf(true) }
    var heightCm by remember { mutableStateOf("") }
    var heightFeet by remember { mutableStateOf("") }
    var heightInches by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tell us about you",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = NeonGreen
        )
        Spacer(modifier = Modifier.height(16.dp))

        SetupTextField(value = name, onValueChange = { name = it }, label = "Name", capitalization = KeyboardCapitalization.Words)
        Spacer(modifier = Modifier.height(12.dp))
        SetupTextField(value = age, onValueChange = { age = it }, label = "Age", keyboardType = KeyboardType.Number)
        Spacer(modifier = Modifier.height(12.dp))
        
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
             SetupTextField(value = heightCm, onValueChange = { heightCm = it }, label = "Height (cm)", keyboardType = KeyboardType.Number)
        } else {
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 SetupTextField(value = heightFeet, onValueChange = { heightFeet = it }, label = "Feet", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                 SetupTextField(value = heightInches, onValueChange = { heightInches = it }, label = "Inches", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
             }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SetupTextField(value = currentWeight, onValueChange = { currentWeight = it }, label = "Current Weight (kg)", keyboardType = KeyboardType.Decimal)
        Spacer(modifier = Modifier.height(12.dp))
        SetupTextField(value = targetWeight, onValueChange = { targetWeight = it }, label = "Target Weight (kg)", keyboardType = KeyboardType.Decimal)


        Spacer(modifier = Modifier.height(32.dp))

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
                saveDataAndNavigate(context, navController, name, age, finalHeight, currentWeight, targetWeight) 
            },
            enabled = name.isNotBlank() && age.isNotBlank() && currentWeight.isNotBlank() && targetWeight.isNotBlank() && ((isCm && heightCm.isNotBlank()) || (!isCm && heightFeet.isNotBlank())),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                disabledContainerColor = NeonGreen.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Continue",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SetupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
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
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        modifier = modifier
    )
}

fun saveDataAndNavigate(context: Context, navController: NavController, name: String, age: String, height: String, weight: String, targetWeight: String) {
    val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("user_name", name)
        putString("age", age)
        putString("height", height)
        putString("weight", weight)
        putString("target_weight", targetWeight)
        putBoolean("onboarding_completed", true)
        apply()
    }
    navController.navigate(Screen.Dashboard.route) {
        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
        popUpTo(Screen.Onboarding.route) { inclusive = true }
    }
}
