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

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState = _forgotPasswordState.asStateFlow()

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

    fun forgotPassword(email: String, newPassword: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            val result = repository.forgotPassword(email = email, newPassword = newPassword)
            if (result.isSuccess) {
                _forgotPasswordState.value = ForgotPasswordState.Success(
                    result.getOrNull() ?: "Password reset successful"
                )
            } else {
                _forgotPasswordState.value = ForgotPasswordState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to reset password"
                )
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}
