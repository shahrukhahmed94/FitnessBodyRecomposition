package com.tsapps.fitnessbodyrecomposition.ui.onboarding

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.tsapps.fitnessbodyrecomposition.data.model.User
import com.tsapps.fitnessbodyrecomposition.data.repository.FirestoreService
import com.tsapps.fitnessbodyrecomposition.navigation.Screen
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.compose.koinInject
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestoreService: FirestoreService = koinInject()

    // State
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isCm by remember { mutableStateOf(true) }
    var heightCm by remember { mutableStateOf("") }
    var heightFeet by remember { mutableStateOf("") }
    var heightInches by remember { mutableStateOf("") }
    var currentWeight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }

    val steps = listOf("Name", "Age", "Height", "Current Weight", "Target Weight")
    val pagerState = rememberPagerState(pageCount = { steps.size })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            SetupBottomBar(
                currentPage = pagerState.currentPage,
                totalSteps = steps.size,
                isNextEnabled = isStepValid(pagerState.currentPage, name, age, isCm, heightCm, heightFeet, currentWeight, targetWeight),
                onNext = {
                    if (pagerState.currentPage < steps.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        // Final Submit
                        val finalHeight = if (isCm) heightCm else {
                            val ft = heightFeet.toDoubleOrNull() ?: 0.0
                            val inch = heightInches.toDoubleOrNull() ?: 0.0
                            ((ft * 30.48) + (inch * 2.54)).toInt().toString()
                        }
                        
                        scope.launch {
                            val auth = FirebaseAuth.getInstance()
                            val userId = auth.currentUser?.uid ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                            val userEmail = auth.currentUser?.email ?: ""
                            
                            val fcmToken = try {
                                FirebaseMessaging.getInstance().token.await()
                            } catch (e: Exception) {
                                ""
                            }
                            val user = User(
                                id = userId,
                                name = name,
                                email = userEmail,
                                age = age,
                                height = finalHeight,
                                weight = currentWeight,
                                targetWeight = targetWeight,
                                fcmToken = fcmToken
                            )
                            firestoreService.saveUser(user)
                            saveDataAndNavigate(context, navController, name, userEmail, age, finalHeight, currentWeight, targetWeight)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1).toFloat() / steps.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeonGreen,
                trackColor = TextGrey.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false // Control via buttons for focused flow
            ) { page ->
                when (page) {
                    0 -> StepContent(
                        title = "What's your name?",
                        subtitle = "We'd love to know who we're training with."
                    ) {
                        StepTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Enter your name",
                            capitalization = KeyboardCapitalization.Words
                        )
                    }
                    1 -> StepContent(
                        title = "How old are you?",
                        subtitle = "Age helps us calculate your metabolism accurately."
                    ) {
                        StepTextField(
                            value = age,
                            onValueChange = { if (it.length <= 3) age = it },
                            placeholder = "Years",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    2 -> StepContent(
                        title = "How tall are you?",
                        subtitle = "Select your preferred unit."
                    ) {
                        HeightStep(
                            isCm = isCm,
                            onUnitChange = { isCm = it },
                            cmValue = heightCm,
                            onCmChange = { heightCm = it },
                            feetValue = heightFeet,
                            onFeetChange = { heightFeet = it },
                            inchValue = heightInches,
                            onInchChange = { heightInches = it }
                        )
                    }
                    3 -> StepContent(
                        title = "Current weight?",
                        subtitle = "Be honest! It's our starting point."
                    ) {
                        StepTextField(
                            value = currentWeight,
                            onValueChange = { currentWeight = it },
                            placeholder = "kg",
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                    4 -> StepContent(
                        title = "Target weight?",
                        subtitle = "Where do you want to be?"
                    ) {
                        StepTextField(
                            value = targetWeight,
                            onValueChange = { targetWeight = it },
                            placeholder = "kg",
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepContent(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = NeonGreen,
            lineHeight = 40.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextGrey
        )
        Spacer(modifier = Modifier.height(48.dp))
        content()
    }
}

@Composable
fun StepTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGrey.copy(alpha = 0.5f)) },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            textAlign = TextAlign.Start
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = NeonGreen,
            unfocusedIndicatorColor = TextGrey,
            cursorColor = NeonGreen
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HeightStep(
    isCm: Boolean,
    onUnitChange: (Boolean) -> Unit,
    cmValue: String,
    onCmChange: (String) -> Unit,
    feetValue: String,
    onFeetChange: (String) -> Unit,
    inchValue: String,
    onInchChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .background(TextGrey.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            UnitButton(selected = isCm, text = "CM", onClick = { onUnitChange(true) })
            UnitButton(selected = !isCm, text = "FT/IN", onClick = { onUnitChange(false) })
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isCm) {
            StepTextField(value = cmValue, onValueChange = onCmChange, placeholder = "175", keyboardType = KeyboardType.Number)
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StepTextField(value = feetValue, onValueChange = onFeetChange, placeholder = "FT", keyboardType = KeyboardType.Number)
                }
                Box(modifier = Modifier.weight(1f)) {
                    StepTextField(value = inchValue, onValueChange = onInchChange, placeholder = "IN", keyboardType = KeyboardType.Number)
                }
            }
        }
    }
}

@Composable
fun UnitButton(selected: Boolean, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) NeonGreen else Color.Transparent,
            contentColor = if (selected) Color.Black else TextGrey
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = null,
        modifier = Modifier.height(40.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SetupBottomBar(
    currentPage: Int,
    totalSteps: Int,
    isNextEnabled: Boolean,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        FloatingActionButton(
            onClick = { if (isNextEnabled) onNext() },
            containerColor = if (isNextEnabled) NeonGreen else TextGrey.copy(alpha = 0.3f),
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = if (currentPage == totalSteps - 1) Icons.Default.Check else Icons.Default.ArrowForward,
                contentDescription = "Next"
            )
        }
    }
}

private fun isStepValid(
    page: Int,
    name: String,
    age: String,
    isCm: Boolean,
    heightCm: String,
    heightFeet: String,
    currentWeight: String,
    targetWeight: String
): Boolean {
    return when (page) {
        0 -> name.isNotBlank()
        1 -> age.isNotBlank()
        2 -> if (isCm) heightCm.isNotBlank() else heightFeet.isNotBlank()
        3 -> currentWeight.isNotBlank()
        4 -> targetWeight.isNotBlank()
        else -> false
    }
}

private fun saveDataAndNavigate(context: Context, navController: NavController, name: String, email: String, age: String, height: String, weight: String, targetWeight: String) {
    val sharedPref = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("user_name", name)
        putString("user_email", email)
        putString("age", age)
        putString("height", height)
        putString("weight", weight)
        putString("target_weight", targetWeight)
        putBoolean("onboarding_completed", true)
        putBoolean("is_guest", false)
        apply()
    }
    navController.navigate(Screen.Dashboard.route) {
        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
        popUpTo(Screen.Onboarding.route) { inclusive = true }
        popUpTo(Screen.Welcome.route) { inclusive = true }
        popUpTo(Screen.SignUp.route) { inclusive = true }
    }
}
