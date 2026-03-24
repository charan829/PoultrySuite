package com.simats.poultrysuite.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val state by viewModel.changePasswordState.collectAsState()
    val primaryBlue = Color(0xFF1565C0)

    LaunchedEffect(state) {
        when (val s = state) {
            is ChangePasswordState.Success -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.resetChangePasswordState()
                navController.popBackStack()
            }
            is ChangePasswordState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                viewModel.resetChangePasswordState()
            }
            else -> Unit
        }
    }

    val passwordsMatch = newPassword == confirmPassword
    val canSubmit =
        currentPassword.isNotBlank() &&
            newPassword.length >= 5 &&
            confirmPassword.isNotBlank() &&
            passwordsMatch &&
            state !is ChangePasswordState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            PasswordField(
                label = "Current Password",
                value = currentPassword,
                onValueChange = { currentPassword = it },
                isVisible = currentVisible,
                onToggleVisibility = { currentVisible = !currentVisible },
                primaryBlue = primaryBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                label = "New Password",
                value = newPassword,
                onValueChange = { newPassword = it },
                isVisible = newVisible,
                onToggleVisibility = { newVisible = !newVisible },
                primaryBlue = primaryBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                label = "Confirm New Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                isVisible = confirmVisible,
                onToggleVisibility = { confirmVisible = !confirmVisible },
                primaryBlue = primaryBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (confirmPassword.isNotBlank() && !passwordsMatch) {
                Text(
                    text = "New passwords do not match",
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "New password must be at least 5 characters.",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.changePassword(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
            ) {
                if (state is ChangePasswordState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update Password", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    primaryBlue: Color
) {
    Text(label, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isVisible) "Hide password" else "Show password"
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryBlue,
            focusedLabelColor = primaryBlue,
            unfocusedBorderColor = Color.LightGray
        )
    )
}
