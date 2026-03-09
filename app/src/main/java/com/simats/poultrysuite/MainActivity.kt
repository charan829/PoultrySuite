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
import androidx.navigation.navArgument
import androidx.navigation.NavType
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
    NavHost(navController = navController, startDestination = com.simats.poultrysuite.ui.navigation.Screen.Splash.route) {
        composable(com.simats.poultrysuite.ui.navigation.Screen.Splash.route) {
            com.simats.poultrysuite.ui.splash.SplashScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.Register.route) {
            com.simats.poultrysuite.ui.auth.RegisterScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.Dashboard.route) {
            com.simats.poultrysuite.ui.dashboard.DashboardScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.Marketplace.route) {
            com.simats.poultrysuite.ui.market.MarketScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.Admin.route) {
            com.simats.poultrysuite.ui.admin.AdminDashboardScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.TotalFarms.route) {
            com.simats.poultrysuite.ui.admin.TotalFarmsScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmDetails.route) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: return@composable
            com.simats.poultrysuite.ui.admin.FarmDetailsScreen(navController = navController, farmId = farmId)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.TotalSales.route) {
            com.simats.poultrysuite.ui.sales.TotalSalesScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.ActiveUsers.route) {
            com.simats.poultrysuite.ui.user.ActiveUsersScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.AdminMarket.route) {
            com.simats.poultrysuite.ui.admin.AdminMarketScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.AdminReports.route) {
            com.simats.poultrysuite.ui.admin.AdminReportsScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.AdminSettings.route) {
            com.simats.poultrysuite.ui.admin.AdminSettingsScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.AdminEditProfile.route) {
            com.simats.poultrysuite.ui.admin.AdminEditProfileScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.AdminChangeEmail.route) {
            com.simats.poultrysuite.ui.admin.AdminChangeEmailScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.AdminChangePassword.route) {
            com.simats.poultrysuite.ui.admin.AdminChangePasswordScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.UserDetails.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            com.simats.poultrysuite.ui.user.UserDetailsScreen(userId = userId, navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerSettings.route) {
            com.simats.poultrysuite.ui.user.FarmerSettingsScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerAccount.route) {
            com.simats.poultrysuite.ui.user.FarmerAccountScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerEditProfile.route) {
            com.simats.poultrysuite.ui.user.FarmerEditProfileScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerAddSale.route) {
            com.simats.poultrysuite.ui.sales.AddSaleScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerInventory.route) {
            com.simats.poultrysuite.ui.inventory.InventoryScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerAddBatch.route) {
            com.simats.poultrysuite.ui.inventory.AddBatchScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerBatchDetail.route) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: return@composable
            com.simats.poultrysuite.ui.inventory.BatchDetailScreen(
                batchId = batchId,
                navController = navController
            )
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerSales.route) {
            com.simats.poultrysuite.ui.sales.SalesScreen(navController = navController)
        }
        composable(
            route = com.simats.poultrysuite.ui.navigation.Screen.TransactionDetails.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: return@composable
            com.simats.poultrysuite.ui.sales.TransactionDetailsScreen(
                transactionId = transactionId,
                navController = navController
            )
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.FarmerCreateListing.route) {
            com.simats.poultrysuite.ui.market.CreateListingScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.Analytics.route) {
            com.simats.poultrysuite.ui.dashboard.analytics.AnalyticsScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.AddExpense.route) {
            com.simats.poultrysuite.ui.dashboard.analytics.AddExpenseScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.CustomerDashboard.route) {
            com.simats.poultrysuite.ui.customer.CustomerMarketplaceScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.CustomerOrders.route) {
            com.simats.poultrysuite.ui.customer.CustomerOrdersScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.CustomerProfile.route) {
            com.simats.poultrysuite.ui.profile.CustomerProfileScreen(navController = navController)
        }
        composable(
            route = com.simats.poultrysuite.ui.navigation.Screen.CustomerProductDetails.route,
            arguments = listOf(androidx.navigation.navArgument("productId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            com.simats.poultrysuite.ui.customer.CustomerProductDetailsScreen(navController = navController, productId = productId)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.CustomerOrderSuccess.route) {
            com.simats.poultrysuite.ui.customer.CustomerOrderSuccessScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.CustomerAccount.route) {
            com.simats.poultrysuite.ui.profile.CustomerAccountScreen(navController = navController)
        }
        composable(com.simats.poultrysuite.ui.navigation.Screen.CustomerEditProfile.route) {
            com.simats.poultrysuite.ui.profile.CustomerEditProfileScreen(navController = navController)
        }
    }
}