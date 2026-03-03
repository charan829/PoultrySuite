package com.simats.poultrysuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.poultrysuite.ui.auth.LoginScreen
import com.simats.poultrysuite.ui.navigation.Screen
import com.simats.poultrysuite.ui.user.ActiveUsersScreen
import com.simats.poultrysuite.ui.theme.PoultrysuiteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoultrysuiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            com.simats.poultrysuite.ui.splash.SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            com.simats.poultrysuite.ui.auth.RegisterScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            com.simats.poultrysuite.ui.dashboard.DashboardScreen(navController = navController)
        }
        composable(Screen.Marketplace.route) {
            com.simats.poultrysuite.ui.market.MarketScreen(navController = navController)
        }
        composable(Screen.Admin.route) {
            com.simats.poultrysuite.ui.admin.AdminDashboardScreen(navController = navController)
        }
        composable(Screen.TotalFarms.route) {
            com.simats.poultrysuite.ui.admin.TotalFarmsScreen(navController = navController)
        }
        composable(Screen.FarmDetails.route) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            com.simats.poultrysuite.ui.admin.FarmDetailsScreen(navController = navController, farmId = farmId)
        }
        composable(Screen.TotalSales.route) {
            com.simats.poultrysuite.ui.sales.TotalSalesScreen(navController = navController)
        }
        composable(Screen.TransactionDetails.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: return@composable
            com.simats.poultrysuite.ui.sales.TransactionDetailsScreen(transactionId = transactionId, navController = navController)
        }
        composable(Screen.ActiveUsers.route) {
            com.simats.poultrysuite.ui.user.ActiveUsersScreen(navController = navController)
        }
        composable(Screen.AdminMarket.route) {
            com.simats.poultrysuite.ui.admin.AdminMarketScreen(navController = navController)
        }
        composable(Screen.AdminReports.route) {
            com.simats.poultrysuite.ui.admin.AdminReportsScreen(navController = navController)
        }
        composable(Screen.AdminSettings.route) {
            com.simats.poultrysuite.ui.admin.AdminSettingsScreen(navController = navController)
        }
        composable(Screen.AdminEditProfile.route) {
            com.simats.poultrysuite.ui.admin.AdminEditProfileScreen(navController = navController)
        }
        composable(Screen.AdminChangeEmail.route) {
            com.simats.poultrysuite.ui.admin.AdminChangeEmailScreen(navController = navController)
        }
        composable(Screen.AdminChangePassword.route) {
            com.simats.poultrysuite.ui.admin.AdminChangePasswordScreen(navController = navController)
        }
        composable(Screen.UserDetails.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            com.simats.poultrysuite.ui.user.UserDetailsScreen(userId = userId, navController = navController)
        }
    }
}