package com.simats.poultrysuite.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Marketplace : Screen("marketplace")
    object Admin : Screen("admin")
    object TotalFarms : Screen("total_farms")
    object TotalSales : Screen("total_sales")
    object FarmDetails : Screen("farm_details/{farmId}") {
        fun createRoute(farmId: String) = "farm_details/$farmId"
    }
    object TransactionDetails : Screen("transaction_details/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_details/$transactionId"
    }
    object ActiveUsers : Screen("active_users")
    object UserDetails : Screen("user_details/{userId}") {
        fun createRoute(userId: String) = "user_details/$userId"
    }
    object AdminMarket : Screen("admin_market")
    object AdminReports : Screen("admin_reports")
    object AdminSettings : Screen("admin_settings")
    object AdminEditProfile : Screen("admin_edit_profile")
    object Splash : Screen("splash")
    object AdminChangeEmail : Screen("admin_change_email")
    object AdminChangePassword : Screen("admin_change_password")
}
