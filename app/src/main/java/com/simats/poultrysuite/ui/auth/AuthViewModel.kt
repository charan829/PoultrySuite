package com.simats.poultrysuite.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.simats.poultrysuite.data.local.SessionManager

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, pass: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.login(email, pass)
            if (result.isSuccess) {
                val response = result.getOrNull()
                val role = response?.role ?: "CUSTOMER"
                // Token and Role are already saved in Repository
                _loginState.value = LoginState.Success(role)
            } else {
                _loginState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    private val _registerState = MutableStateFlow<LoginState>(LoginState.Idle)
    val registerState = _registerState.asStateFlow()

    private val _forgotPasswordOtpState = MutableStateFlow<ForgotPasswordOtpState>(ForgotPasswordOtpState.Idle)
    val forgotPasswordOtpState = _forgotPasswordOtpState.asStateFlow()

    private val _forgotPasswordResetState = MutableStateFlow<ForgotPasswordResetState>(ForgotPasswordResetState.Idle)
    val forgotPasswordResetState = _forgotPasswordResetState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState = _changePasswordState.asStateFlow()

    fun register(name: String, email: String, pass: String, phone: String, role: String) {
        _registerState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.register(name, email, pass, role, phone)
            if (result.isSuccess) {
                // Register doesn't return token in this implementation, so we just succeed.
                // User will need to login.
                _registerState.value = LoginState.Success(role) 
            } else {
                _registerState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun requestForgotPasswordOtp(email: String) {
        _forgotPasswordOtpState.value = ForgotPasswordOtpState.Loading
        viewModelScope.launch {
            val result = repository.requestForgotPasswordOtp(email = email)
            if (result.isSuccess) {
                _forgotPasswordOtpState.value = ForgotPasswordOtpState.Success(
                    result.getOrNull() ?: "OTP sent successfully"
                )
            } else {
                _forgotPasswordOtpState.value = ForgotPasswordOtpState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to send OTP"
                )
            }
        }
    }

    fun verifyForgotPasswordOtp(email: String, otp: String, newPassword: String) {
        _forgotPasswordResetState.value = ForgotPasswordResetState.Loading
        viewModelScope.launch {
            val result = repository.verifyForgotPasswordOtp(email = email, otp = otp, newPassword = newPassword)
            if (result.isSuccess) {
                _forgotPasswordResetState.value = ForgotPasswordResetState.Success(
                    result.getOrNull() ?: "Password reset successful"
                )
            } else {
                _forgotPasswordResetState.value = ForgotPasswordResetState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to verify OTP"
                )
            }
        }
    }

    fun resetForgotPasswordOtpState() {
        _forgotPasswordOtpState.value = ForgotPasswordOtpState.Idle
    }

    fun resetForgotPasswordResetState() {
        _forgotPasswordResetState.value = ForgotPasswordResetState.Idle
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        _changePasswordState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            val result = repository.changePassword(currentPassword = currentPassword, newPassword = newPassword)
            if (result.isSuccess) {
                _changePasswordState.value = ChangePasswordState.Success(
                    result.getOrNull() ?: "Password updated successfully"
                )
            } else {
                _changePasswordState.value = ChangePasswordState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to change password"
                )
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class ForgotPasswordOtpState {
    object Idle : ForgotPasswordOtpState()
    object Loading : ForgotPasswordOtpState()
    data class Success(val message: String) : ForgotPasswordOtpState()
    data class Error(val message: String) : ForgotPasswordOtpState()
}

sealed class ForgotPasswordResetState {
    object Idle : ForgotPasswordResetState()
    object Loading : ForgotPasswordResetState()
    data class Success(val message: String) : ForgotPasswordResetState()
    data class Error(val message: String) : ForgotPasswordResetState()
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}
