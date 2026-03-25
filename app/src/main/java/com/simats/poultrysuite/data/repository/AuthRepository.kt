package com.simats.poultrysuite.data.repository

import com.simats.poultrysuite.data.local.SessionManager
import com.simats.poultrysuite.data.model.ChangePasswordRequest
import com.simats.poultrysuite.data.model.ForgotPasswordOtpRequest
import com.simats.poultrysuite.data.model.ForgotPasswordOtpVerifyRequest
import com.simats.poultrysuite.data.remote.PoultryApi
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: PoultryApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<com.simats.poultrysuite.data.model.LoginResponse> {
        return try {
            val request = com.simats.poultrysuite.data.model.LoginRequest(email.trim().lowercase(), password)
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
            val request = com.simats.poultrysuite.data.model.RegisterRequest(name, email.trim().lowercase(), password, role, phone)
            val response = api.register(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestForgotPasswordOtp(email: String): Result<String> {
        return try {
            val response = api.requestForgotPasswordOtp(
                ForgotPasswordOtpRequest(email = email.trim().lowercase())
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "OTP sent successfully")
            } else {
                val body = response.errorBody()?.string()
                val message = parseErrorMessage(body) ?: "Failed to send OTP"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyForgotPasswordOtp(email: String, otp: String, newPassword: String): Result<String> {
        return try {
            val response = api.verifyForgotPasswordOtp(
                ForgotPasswordOtpVerifyRequest(
                    email = email.trim().lowercase(),
                    otp = otp.trim(),
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Password reset successful")
            } else {
                val body = response.errorBody()?.string()
                val message = parseErrorMessage(body) ?: "Failed to verify OTP"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        return try {
            val response = api.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Password updated successfully")
            } else {
                val body = response.errorBody()?.string()
                val message = parseErrorMessage(body) ?: "Failed to change password"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            JSONObject(errorBody).optString("error").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
    
    suspend fun logout() {
        sessionManager.clearSession()
    }
}
