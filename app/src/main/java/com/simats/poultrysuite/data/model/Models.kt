package com.simats.poultrysuite.data.model

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val role: String
)

data class Farm(
    val id: String,
    val ownerId: String,
    val name: String,
    val location: String?,
    val batches: List<Batch>?,
    val inventory: List<Inventory>?,
    
    // KPI Metrics
    val totalBirds: Int? = 0,
    val birdsTrend: Double? = 0.0,
    val eggsToday: Int? = 0,
    val eggsTrend: Double? = 0.0,
    val feedRemaining: Double? = 0.0,
    val feedTrend: Double? = 0.0,
    val todayRevenue: Double? = 0.0,
    val revenueTrend: Double? = 0.0,
    
    // Charts
    val weeklyProductionValues: List<Double>? = null,
    val monthlyRevenueValues: List<Double>? = null
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

data class FarmerProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val farmName: String,
    val location: String
)

data class FarmerProfileUpdateRequest(
    val fullName: String,
    val phone: String,
    val farmName: String,
    val location: String
)

data class SaleRequest(
    val productType: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val buyerName: String,
    val notes: String,
    val paymentStatus: String = "Paid"
)

data class SaleRecord(
    val id: String,
    val productType: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalPrice: Double,
    val buyerName: String,
    val notes: String,
    val paymentStatus: String,
    val status: String,
    val createdAt: String
)

data class InventoryBatch(
    val id: String,
    val name: String,
    val type: String,
    val count: Int,
    val ageDays: Int,
    val weeksOld: Int,
    val mortalityRate: Double,
    val status: String,
    val startedAt: String
)

data class InventoryResponse(
    val batches: List<InventoryBatch>,
    val feedKg: Double,
    val medicineCount: Int
)

data class MortalityRecord(
    val id: String,
    val count: Int,
    val cause: String,
    val date: String
)

data class VaccinationRecord(
    val id: String,
    val name: String,
    val scheduledDate: String,
    val status: String
)

data class FeedLog(
    val id: String,
    val amountKg: Double,
    val date: String,
    val notes: String?
)

data class BatchDetail(
    val id: String,
    val name: String,
    val type: String,
    val count: Int,
    val ageDays: Int,
    val weeksOld: Int,
    val mortality: Int,
    val mortalityRate: Double,
    val startedAt: String,
    val mortalityRecords: List<MortalityRecord>,
    val vaccinationRecords: List<VaccinationRecord>,
    val feedLogs: List<FeedLog>,
    val totalFeedKg: Double,
    val avgDailyFeedKg: Double
)

data class OrderDetail(
    val id: String,
    val productType: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalPrice: Double,
    val buyerName: String,
    val notes: String?,
    val paymentStatus: String,
    val status: String,
    val createdAt: String,
    val buyerPhone: String?,
    val buyerAddress: String?,
    val buyerType: String,
    val paymentMethod: String,
    val dueDate: String?
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
    val status: String,
    val createdAt: String? = null,
    val product: ProductRequest? = null,
    val buyerName: String? = null,
    val notes: String? = null,
    val paymentStatus: String? = null,
    val purchaseType: String? = null,
    val deliveryAddress: String? = null,
    val isReviewed: Boolean? = false
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

data class Review(
    val id: String,
    val rating: Int,
    val comment: String?,
    val customerName: String,
    val createdAt: String
)

data class ReviewRequest(
    val orderId: String,
    val rating: Int,
    val comment: String?
)

data class CanReviewResponse(
    val canReview: Boolean,
    val orderId: String?
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
data class ForgotPasswordOtpRequest(val email: String)
data class ForgotPasswordOtpVerifyRequest(val email: String, val otp: String, val newPassword: String)
data class ForgotPasswordResponse(val message: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class ChangePasswordResponse(val message: String)

// ─── Messaging ────────────────────────────────────────────────────

data class ConversationSummary(
    val id: String,
    val farmId: String,
    val farmName: String,
    val otherPartyName: String,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int
)

data class ChatMessage(
    val id: String,
    val content: String,
    val senderId: String,
    val isMine: Boolean,
    val isRead: Boolean,
    val createdAt: String
)

data class StartConversationResponse(
    val id: String,
    val farmId: String,
    val farmName: String
)

data class StartFarmerConversationResponse(
    val id: String,
    val partnerName: String
)

data class SendMessageRequest(val content: String)
