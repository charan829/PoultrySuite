package com.simats.poultrysuite.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.R
import com.simats.poultrysuite.ui.auth.AuthViewModel
import com.simats.poultrysuite.ui.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // We can check session here. 
    // For simplicity, let's just wait 2 seconds and then decide based on ViewModel state or SessionManager.
    // Ideally AuthViewModel should have a "checkSession" init block or similar.
    // But since we are already checking flow in other screens, let's just do a simple delay for branding.
    
    LaunchedEffect(true) {
        delay(2000) // Show splash for 2 seconds
        // Navigate to Login for now, or check session if we want auto-login.
        // Let's assume start with Login for this flow as requested "Authentication". 
        // If we want auto-login, we need to peek at valid token. 
        // Let's start with Login to be safe, easier to demonstrate.
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // Design: Blue Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1565C0)), // Blue color similar to screenshot
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // White Container for Logo
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Placeholder for Logo. User needs to provide 'logo' resource.
                    // Using a default icon if logo not found logic isn't easily possible in Compose resource loading without try/catch or logic.
                    // safely we assume user will add it or we use a fallback. 
                    // For now, let's use a standard icon as placeholder.
                    // R.drawable.ic_launcher_foreground might strictly exist but lets try to use a generic icon
                    // OR better: Text emoji as placeholder if image fails? No, let's assume image.
                    // Using user provided logo.jpg
                    Image(
                        painter = painterResource(id = R.drawable.logo), 
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "PoultrySuite",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Smart Poultry Farm Management",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
    }
}
