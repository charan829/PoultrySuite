package com.simats.poultrysuite.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
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
    object AdminNotifications : Screen("admin_notifications")
    object AdminSecurity : Screen("admin_security")
    object FarmerSettings : Screen("farmer_settings")
    object FarmerAccount : Screen("farmer_account")
    object FarmerNotifications : Screen("farmer_notifications")
    object FarmerSecurity : Screen("farmer_security")
    object FarmerEditProfile : Screen("farmer_edit_profile")
    object FarmerAddSale : Screen("farmer_add_sale")
    object FarmerInventory : Screen("farmer_inventory")
    object FarmerAddBatch : Screen("farmer_add_batch")
    object FarmerBatchDetail : Screen("farmer_batch_detail/{batchId}") {
        fun createRoute(batchId: String) = "farmer_batch_detail/$batchId"
    }
    object FarmerSales : Screen("farmer_sales")
    object FarmerCreateListing : Screen("farmer_create_listing")
    object FarmerMessages : Screen("farmer_messages")
    object FarmerChat : Screen("farmer_chat/{conversationId}/{partnerName}") {
        fun createRoute(conversationId: String, partnerName: String) =
            "farmer_chat/$conversationId/${java.net.URLEncoder.encode(partnerName, "UTF-8")}"
    }
    object Analytics : Screen("analytics")
    object AddExpense : Screen("add_expense")
    object CustomerDashboard : Screen("customer_dashboard")
    object CustomerOrders : Screen("customer_orders")
    object CustomerProfile : Screen("customer_profile")
    object CustomerNotifications : Screen("customer_notifications")
    object CustomerSecurity : Screen("customer_security")
    object CustomerProductDetails : Screen("customer_product_details/{productId}") {
        fun createRoute(productId: String) = "customer_product_details/$productId"
    }
    object CustomerFarmProfile : Screen("customer_farm_profile/{farmId}") {
        fun createRoute(farmId: String) = "customer_farm_profile/$farmId"
    }
    object CustomerOrderTracking : Screen("customer_order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "customer_order_tracking/$orderId"
    }
    object CustomerWriteReview : Screen("customer_write_review/{orderId}") {
        fun createRoute(orderId: String) = "customer_write_review/$orderId"
    }
    object CustomerOrderSuccess : Screen("customer_order_success")
    object CustomerAccount : Screen("customer_account")
    object CustomerEditProfile : Screen("customer_edit_profile")
    object CustomerMessages : Screen("customer_messages")
    object CustomerChat : Screen("customer_chat/{conversationId}/{farmName}") {
        fun createRoute(conversationId: String, farmName: String) = "customer_chat/$conversationId/${java.net.URLEncoder.encode(farmName, "UTF-8")}"
    }
}
