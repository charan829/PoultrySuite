package com.simats.poultrysuite.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChangePasswordScreen(
    navController: NavController
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Change Password", 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA) // Light gray background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Current Password", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
            PasswordField(
                value = currentPassword, 
                onValueChange = { currentPassword = it },
                passwordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("New Password", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
            PasswordField(
                value = newPassword, 
                onValueChange = { newPassword = it },
                passwordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Confirm New Password", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
            PasswordField(
                value = confirmPassword, 
                onValueChange = { confirmPassword = it },
                passwordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Password must be at least 8 characters and contain a number and symbol.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0))
            ) {
                Text("Update Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            IconButton(onClick = { onVisibilityChange(!passwordVisible) }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = Color.Gray
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true
    )
}
