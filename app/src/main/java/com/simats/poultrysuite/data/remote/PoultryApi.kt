package com.simats.poultrysuite.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.simats.poultrysuite.data.model.Farm
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
    
    @POST("auth/register")
    suspend fun register(@Body request: com.simats.poultrysuite.data.model.RegisterRequest): com.simats.poultrysuite.data.model.RegisterResponse
    
    @GET("farm/dashboard")
    suspend fun getDashboard(): Farm
    
    @GET("market/listings")
    suspend fun getListings(): List<ProductRequest>
    
    @POST("market/listing")
    suspend fun createListing(@Body request: Map<String, String>): ProductRequest
    
    @POST("market/order")
    suspend fun placeOrder(@Body request: Map<String, String>): Order
    
    @POST("farm/batch")
    suspend fun addBatch(@Body request: Map<String, String>): com.simats.poultrysuite.data.model.Batch
    
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
}

data class UserStatusUpdate(val status: String)
