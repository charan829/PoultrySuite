package com.simats.poultrysuite.data.model

data class Farm(
    val id: String,
    val ownerId: String,
    val name: String,
    val location: String?,
    val batches: List<Batch>?,
    val inventory: List<Inventory>?
)

data class Batch(
    val id: String,
    val farmId: String,
    val type: String,
    val count: Int,
    val ageDays: Int
)

data class Inventory(
    val id: String,
    val farmId: String,
    val feedKg: Double,
    val medicine: String?
)

data class ProductRequest(
    val id: String,
    val farmId: String,
    val type: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val status: String,
    val farm: Farm?
)

data class Order(
    val id: String,
    val customerId: String,
    val productId: String,
    val totalPrice: Double,
    val status: String
)

data class AdminStats(
    val users: Int,
    val farms: Int,
    val orders: Int,
    val totalSales: Double,
    val pendingApprovals: Int,
    val activeNow: Int?,
    val highRiskUsers: Int?,
    val logsToday: String?,
    val userGrowthData: List<Double>?,
    val userGrowthLabels: List<String>?,
    val revenueData: List<Double>?,
    val revenueLabels: List<String>?,
    val usersPercent: String?,
    val highRiskPercent: String?,
    val activePercent: String?,
    val logsPercent: String?
)

data class RecentActivity(
    val id: String,
    val type: String, // "USER", "FARM", "ORDER"
    val message: String,
    val timestamp: String // ISO String
)

data class AdminFarmItem(
    val id: String,
    val name: String,
    val ownerName: String,
    val location: String,
    val totalBirds: Int,
    val scale: String,
    val initial: Char
)
data class FarmDetails(
    val id: String,
    val name: String,
    val ownerName: String,
    val location: String,
    val phone: String,
    val email: String,
    val joinedDate: String,
    val status: String,
    val scale: String,
    val totalBirds: Int,
    val monthlyRevenue: Double,
    val productsCount: Int,
    val productTypes: List<String>,
    val productionGraph: List<Double>
)

data class AdminSalesStats(
    val todayRevenue: Double,
    val todayOrders: Int,
    val weeklyRevenue: List<WeeklyRevenue>,
    val recentTransactions: List<TransactionItem>
)

data class WeeklyRevenue(
    val day: String,
    val revenue: Double
)

data class TransactionItem(
    val id: String,
    val customerName: String,
    val farmName: String,
    val items: String,
    val amount: Double,
    val status: String,
    val date: String
)

data class TransactionDetails(
    val id: String,
    val date: String,
    val status: String,
    val amount: Double,
    val customer: CustomerInfo,
    val product: ProductInfo
)

data class CustomerInfo(
    val name: String,
    val email: String,
    val phone: String?
)

data class ProductInfo(
    val name: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val farm: String,
    val location: String?
)

data class AdminUserItem(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val lastActive: String,
    val initial: Char
)

data class UserDetails(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val location: String,
    val joinedDate: String,
    val status: String,
    val totalOrders: Int,
    val totalRevenue: Double,
    val activityGraph: List<Int>,
    val daysActive: Int,
    val riskText: String,
    val riskColorHex: String?,
    val recentActivity: List<RecentActivityItem>
)

data class RecentActivityItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val type: String // "ORDER", "LISTING", "SYSTEM", "ALERT"
)

data class AdminReportsResponse(
    val sales: SalesReport,
    val userGrowth: UserGrowthReport,
    val marketplace: MarketplaceReport
)

data class SalesReport(
    val monthly: List<Double>,
    val totalYtd: Double
)

data class UserGrowthReport(
    val monthly: List<Int>,
    val totalUsers: Int
)

data class MarketplaceReport(
    val activeListings: Int,
    val completedOrders: Int,
    val avgOrderValue: Double
)

data class FlaggedItem(
    val id: String,
    val itemName: String,
    val farmName: String,
    val price: Double,
    val reason: String,
    val severity: String // "High", "Medium", "Low"
)

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val role: String, val userId: String, val name: String?)
data class RegisterRequest(val name: String, val email: String, val password: String, val role: String, val phone: String)
data class RegisterResponse(val message: String, val userId: String)
