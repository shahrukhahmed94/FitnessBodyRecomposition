package com.tsapps.fitnessbodyrecomposition.ui.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import com.tsapps.fitnessbodyrecomposition.R
import com.tsapps.fitnessbodyrecomposition.navigation.Screen
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextGrey
import com.tsapps.fitnessbodyrecomposition.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavController) {
    val items = listOf(
        OnboardingItem(
            title = "Track Progress",
            description = "Monitor your weight, calories, and macros effortlessly.",
            accentColor = NeonGreen,
            imageRes = R.drawable.onboarding_1_progress
        ),
        OnboardingItem(
            title = "Analyze Diet",
            description = "Get insights into your nutrition to optimize your gains.",
            accentColor = Color(0xFF00E5FF),
            imageRes = R.drawable.onboarding_2_diet
        ),
        OnboardingItem(
            title = "Achieve Goals",
            description = "Reach your body recomposition goals with guided plans.",
            accentColor = Color(0xFFFF4081),
            imageRes = R.drawable.onboarding_3_goals
        )
    )
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { completeOnboarding(context, navController) }) {
                Text("Skip", color = TextGrey)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            OnboardingPage(item = items[page])
        }

        Spacer(modifier = Modifier.weight(1f))

        // Indicators
        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(items.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val color = if (isSelected) items[iteration].accentColor else TextGrey.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (isSelected) 12.dp else 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next / Get Started Button
        Button(
            onClick = {
                if (pagerState.currentPage < items.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    completeOnboarding(context, navController)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (pagerState.currentPage == items.size - 1) "Get Started" else "Next",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OnboardingPage(item: OnboardingItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Image card with gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(MaterialTheme.shapes.large)
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient overlay at the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                item.accentColor.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextGrey,
            textAlign = TextAlign.Center
        )
    }
}

fun completeOnboarding(context: Context, navController: NavController) {
    navController.navigate(Screen.ProfileSetup.route) {
        popUpTo(Screen.Onboarding.route) { inclusive = true }
    }
}

data class OnboardingItem(
    val title: String,
    val description: String,
    val accentColor: Color,
    val imageRes: Int
)
