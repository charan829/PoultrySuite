package com.simats.poultrysuite.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.simats.poultrysuite.data.model.Farm
import com.simats.poultrysuite.data.model.OrderDetail
import com.simats.poultrysuite.data.model.ProductRequest
import com.simats.poultrysuite.data.model.Order
import com.simats.poultrysuite.data.model.AdminStats
import com.simats.poultrysuite.data.model.AdminSalesStats
import com.simats.poultrysuite.data.model.TransactionDetails
import com.simats.poultrysuite.data.model.AdminUserItem
import com.simats.poultrysuite.data.model.UserDetails
import com.simats.poultrysuite.data.model.AdminFarmItem
import com.simats.poultrysuite.data.model.FarmDetails
import com.simats.poultrysuite.data.model.AdminReportsResponse



interface PoultryApi {
    @POST("auth/login")
    suspend fun login(@Body request: com.simats.poultrysuite.data.model.LoginRequest): com.simats.poultrysuite.data.model.LoginResponse
    
    @GET("auth/me")
    suspend fun getProfile(): com.simats.poultrysuite.data.model.UserResponse

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: Map<String, String>): com.simats.poultrysuite.data.model.UserResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: com.simats.poultrysuite.data.model.RegisterRequest): com.simats.poultrysuite.data.model.RegisterResponse

    @POST("auth/forgot-password/request-otp")
    suspend fun requestForgotPasswordOtp(@Body request: com.simats.poultrysuite.data.model.ForgotPasswordOtpRequest): retrofit2.Response<com.simats.poultrysuite.data.model.ForgotPasswordResponse>

    @POST("auth/forgot-password/verify-otp")
    suspend fun verifyForgotPasswordOtp(@Body request: com.simats.poultrysuite.data.model.ForgotPasswordOtpVerifyRequest): retrofit2.Response<com.simats.poultrysuite.data.model.ForgotPasswordResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: com.simats.poultrysuite.data.model.ChangePasswordRequest): retrofit2.Response<com.simats.poultrysuite.data.model.ChangePasswordResponse>
    
    @GET("farm/dashboard")
    suspend fun getDashboard(): Farm
    
    @GET("farm/profile")
    suspend fun getFarmerProfile(): com.simats.poultrysuite.data.model.FarmerProfile
    
    @PUT("farm/profile")
    suspend fun updateFarmerProfile(@Body request: com.simats.poultrysuite.data.model.FarmerProfileUpdateRequest): retrofit2.Response<Unit>
    
    @POST("farm/sale")
    suspend fun addSale(@Body request: com.simats.poultrysuite.data.model.SaleRequest): retrofit2.Response<com.simats.poultrysuite.data.model.SaleRecord>
    
    @GET("farm/sales")
    suspend fun getSales(): List<com.simats.poultrysuite.data.model.SaleRecord>

    @GET("farm/sale/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): OrderDetail

    @PATCH("farm/sale/{id}/mark-paid")
    suspend fun markSaleAsPaid(@Path("id") id: String): retrofit2.Response<Unit>

    @PATCH("farm/order/{id}/complete")
    suspend fun markOrderComplete(@Path("id") id: String): retrofit2.Response<Unit>

    @GET("farm/inventory")
    suspend fun getInventory(): com.simats.poultrysuite.data.model.InventoryResponse

    @GET("farm/batch/{id}")
    suspend fun getBatchDetail(@retrofit2.http.Path("id") id: String): com.simats.poultrysuite.data.model.BatchDetail
    
    @GET("market/listings")
    suspend fun getListings(): List<ProductRequest>
    
    @POST("market/listing")
    suspend fun createListing(@Body request: Map<String, String>): ProductRequest
    
    @GET("market/my-orders")
    suspend fun getMyOrders(): List<Order>
    
    @POST("market/order")
    suspend fun placeOrder(@Body request: Map<String, String>): Order
    
    @POST("farm/batch")
    suspend fun addBatch(@Body request: Map<String, String>): com.simats.poultrysuite.data.model.Batch
    
    @POST("farm/batch/{id}/mortality")
    suspend fun logMortality(
        @retrofit2.http.Path("id") id: String,
        @Body request: Map<String, String>
    ): retrofit2.Response<Unit>

    @POST("farm/batch/{id}/vaccination")
    suspend fun logVaccination(
        @retrofit2.http.Path("id") id: String,
        @Body request: Map<String, String>
    ): retrofit2.Response<Unit>

    @POST("farm/batch/{id}/feed")
    suspend fun logFeed(
        @retrofit2.http.Path("id") id: String,
        @Body request: Map<String, String>
    ): retrofit2.Response<Unit>

    @POST("farm/expense")
    suspend fun addExpense(@Body request: Map<String, String>): retrofit2.Response<Unit>

    @GET("farm/analytics")
    suspend fun getAnalytics(): com.simats.poultrysuite.data.model.AnalyticsResponse

    @GET("admin/stats")
    suspend fun getAdminStats(): AdminStats

    @GET("admin/farms")
    suspend fun getFarms(): List<AdminFarmItem>

    @GET("admin/farm/{id}")
    suspend fun getFarmDetails(@Path("id") id: String): FarmDetails

    @GET("admin/sales")
    suspend fun getAdminSales(): AdminSalesStats

    @GET("admin/transaction/{id}")
    suspend fun getTransactionDetails(@Path("id") id: String): TransactionDetails

    @GET("admin/users")
    suspend fun getUsers(): List<AdminUserItem>

    @GET("admin/user/{id}")
    suspend fun getUserDetails(@Path("id") id: String): UserDetails

    @PUT("admin/user/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: String, @Body status: UserStatusUpdate): AdminUserItem

    @GET("admin/reports")
    suspend fun getReports(): AdminReportsResponse

        @POST("review")
        suspend fun submitReview(@Body request: com.simats.poultrysuite.data.model.ReviewRequest): retrofit2.Response<Unit>

        @GET("review/farm/{farmId}")
        suspend fun getFarmReviews(@Path("farmId") farmId: String): List<com.simats.poultrysuite.data.model.Review>

    @POST("farm/profile/images")
    suspend fun uploadFarmImages(@Body request: com.simats.poultrysuite.data.model.FarmImageUploadRequest): retrofit2.Response<Unit>

        @GET("review/can-review/{farmId}")
        suspend fun canReviewFarm(@Path("farmId") farmId: String): com.simats.poultrysuite.data.model.CanReviewResponse

        // ─── Messaging ────────────────────────────────────────────────

        @GET("messages/conversations")
        suspend fun getConversations(): List<com.simats.poultrysuite.data.model.ConversationSummary>

        @POST("messages/start/{farmId}")
        suspend fun startConversation(@Path("farmId") farmId: String): com.simats.poultrysuite.data.model.StartConversationResponse

        @POST("messages/start/order/{orderId}")
        suspend fun startConversationFromOrder(@Path("orderId") orderId: String): com.simats.poultrysuite.data.model.StartFarmerConversationResponse

        @GET("messages/{conversationId}")
        suspend fun getMessages(@Path("conversationId") conversationId: String): List<com.simats.poultrysuite.data.model.ChatMessage>

        @POST("messages/{conversationId}")
        suspend fun sendMessage(
            @Path("conversationId") conversationId: String,
            @Body request: com.simats.poultrysuite.data.model.SendMessageRequest
        ): com.simats.poultrysuite.data.model.ChatMessage
}

data class UserStatusUpdate(val status: String)
