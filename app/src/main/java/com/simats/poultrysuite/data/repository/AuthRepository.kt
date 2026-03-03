package com.simats.poultrysuite.data.repository

import com.simats.poultrysuite.data.local.SessionManager
import com.simats.poultrysuite.data.remote.PoultryApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: PoultryApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<com.simats.poultrysuite.data.model.LoginResponse> {
        return try {
            val request = com.simats.poultrysuite.data.model.LoginRequest(email, password)
            val response = api.login(request)
            
            sessionManager.saveAuthToken(response.token)
            sessionManager.saveUserRole(response.role)
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, role: String, phone: String): Result<com.simats.poultrysuite.data.model.RegisterResponse> {
        return try {
            val request = com.simats.poultrysuite.data.model.RegisterRequest(name, email, password, role, phone)
            val response = api.register(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        sessionManager.clearSession()
    }
}
