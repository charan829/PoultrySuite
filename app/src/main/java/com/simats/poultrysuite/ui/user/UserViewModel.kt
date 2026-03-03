package com.simats.poultrysuite.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.AdminUserItem
import com.simats.poultrysuite.data.model.UserDetails
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _usersState = MutableStateFlow<UsersState>(UsersState.Loading)
    val usersState = _usersState.asStateFlow()

    private val _userDetailsState = MutableStateFlow<UserDetailsState>(UserDetailsState.Loading)
    val userDetailsState = _userDetailsState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _usersState.value = UsersState.Loading
                val users = api.getUsers()
                _usersState.value = UsersState.Success(users)
            } catch (e: Exception) {
                _usersState.value = UsersState.Error(e.message ?: "Failed to load users")
            }
        }
    }

    fun loadUserDetails(id: String) {
        viewModelScope.launch {
            try {
                _userDetailsState.value = UserDetailsState.Loading
                val details = api.getUserDetails(id)
                _userDetailsState.value = UserDetailsState.Success(details)
            } catch (e: Exception) {
                _userDetailsState.value = UserDetailsState.Error(e.message ?: "Failed to load user details")
            }
        }
    }

    fun updateUserStatus(userId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                api.updateUserStatus(userId, com.simats.poultrysuite.data.remote.UserStatusUpdate(newStatus))
                loadUsers() // Refresh list
            } catch (e: Exception) {
                // Handle error (show toast or snackbar ideally)
                println("Error updating status: ${e.message}")
            }
        }
    }
}

sealed class UsersState {
    object Loading : UsersState()
    data class Success(val users: List<AdminUserItem>) : UsersState()
    data class Error(val message: String) : UsersState()
}

sealed class UserDetailsState {
    object Loading : UserDetailsState()
    data class Success(val details: UserDetails) : UserDetailsState()
    data class Error(val message: String) : UserDetailsState()
}
