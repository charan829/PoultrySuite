package com.simats.poultrysuite.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.AdminUserItem
import com.simats.poultrysuite.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveUsersScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val usersState by viewModel.usersState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Active", "High Risk", "Blocked")

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        bottomBar = {
             com.simats.poultrysuite.ui.admin.AdminBottomNavigation(navController)
        },
        containerColor = Color(0xFFF8F9FA) // Very light gray from mockup
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Custom Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Users",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                        .clickable { 
                            val nextIndex = (filters.indexOf(selectedFilter) + 1) % filters.size
                            selectedFilter = filters[nextIndex]
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt, 
                        contentDescription = "Filter",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search users...", color = Color(0xFFA0AABF)) },
                leadingIcon = { 
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA0AABF)) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF1E293B),
                    unfocusedTextColor = Color(0xFF1E293B),
                    cursorColor = Color(0xFF5C6BC0)
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Filter Chips
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filters) { filterStr ->
                    val isSelected = selectedFilter == filterStr
                    Surface(
                        color = if (isSelected) Color(0xFF5C6BC0) else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { selectedFilter = filterStr },
                        shadowElevation = if (isSelected) 4.dp else 1.dp
                    ) {
                        Text(
                            text = filterStr,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = usersState) {
                is UsersState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF5C6BC0))
                    }
                }
                is UsersState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                    }
                }
                is UsersState.Success -> {
                    val filteredUsers = state.users.filter { user ->
                        val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) ||
                                            user.email.contains(searchQuery, ignoreCase = true)
                        val matchesFilter = when (selectedFilter) {
                            "Active" -> user.status == "ACTIVE"
                            "High Risk" -> user.status == "SUSPENDED" || (user.status == "PENDING") // Using pending/suspended as risk
                            "Blocked" -> user.status == "SUSPENDED"
                            else -> true
                        }
                        matchesSearch && matchesFilter
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // padding for bottom nav
                    ) {
                        items(filteredUsers) { user ->
                            UserListItem(
                                user = user,
                                onClick = { navController.navigate(Screen.UserDetails.createRoute(user.id)) },
                                onStatusChange = { newStatus ->
                                    viewModel.updateUserStatus(user.id, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: AdminUserItem, 
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.initial.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // High Risk Badge next to name
                    if (user.status == "SUSPENDED") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "HIGH\nRISK", // Two lines on mockup
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, lineHeight = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusDotColor = when (user.status) {
                        "ACTIVE" -> Color(0xFF22C55E) // Green
                        "PENDING" -> Color(0xFFF59E0B) // Orange
                        "SUSPENDED" -> Color(0xFFEF4444) // Red
                        else -> Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatRelativeTimeStr(user.lastActive),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            
            // Right Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
