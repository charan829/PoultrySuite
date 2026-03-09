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
    object FarmerSettings : Screen("farmer_settings")
    object FarmerAccount : Screen("farmer_account")
    object FarmerEditProfile : Screen("farmer_edit_profile")
    object FarmerAddSale : Screen("farmer_add_sale")
    object FarmerInventory : Screen("farmer_inventory")
    object FarmerAddBatch : Screen("farmer_add_batch")
    object FarmerBatchDetail : Screen("farmer_batch_detail/{batchId}") {
        fun createRoute(batchId: String) = "farmer_batch_detail/$batchId"
    }
    object FarmerSales : Screen("farmer_sales")
    object FarmerCreateListing : Screen("farmer_create_listing")
    object Analytics : Screen("analytics")
    object AddExpense : Screen("add_expense")
    object CustomerDashboard : Screen("customer_dashboard")
    object CustomerOrders : Screen("customer_orders")
    object CustomerProfile : Screen("customer_profile")
    object CustomerProductDetails : Screen("customer_product_details/{productId}") {
        fun createRoute(productId: String) = "customer_product_details/$productId"
    }
    object CustomerOrderSuccess : Screen("customer_order_success")
    object CustomerAccount : Screen("customer_account")
    object CustomerEditProfile : Screen("customer_edit_profile")
}
