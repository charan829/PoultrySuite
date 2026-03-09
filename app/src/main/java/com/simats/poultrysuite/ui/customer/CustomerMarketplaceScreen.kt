package com.simats.poultrysuite.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simats.poultrysuite.data.model.ProductRequest
import com.simats.poultrysuite.ui.market.MarketState
import com.simats.poultrysuite.ui.market.MarketViewModel
import com.simats.poultrysuite.ui.navigation.Screen

// Pastel background colors for listing cards
private val cardBgColors = listOf(
    Color(0xFFFFF9C4), // yellow
    Color(0xFFE3F2FD), // blue
    Color(0xFFE8F5E9), // green
    Color(0xFFFCE4EC), // pink
    Color(0xFFF3E5F5), // purple
    Color(0xFFE0F7FA), // cyan
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMarketplaceScreen(
    navController: NavController,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val state by viewModel.marketState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Eggs", "Broilers", "Layers", "Chicks")

    // Refresh on resume
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Screen.CustomerDashboard.route) {
            viewModel.loadListings()
        }
    }

    Scaffold(
        bottomBar = { CustomerBottomNavigation(navController) },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Marketplace",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search products...", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clickable { selectedCategory = category }
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF1565C0) else Color(0xFFF8F9FA))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Content Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                when (val s = state) {
                    is MarketState.Loading -> Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Color(0xFF1565C0)) }

                    is MarketState.Error -> Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to load listings", color = Color(0xFF94A3B8))
                            TextButton({ viewModel.loadListings() }) {
                                Text("Retry", color = Color(0xFF1565C0))
                            }
                        }
                    }

                    is MarketState.Success -> {
                        val filteredListings = s.listings.filter { item ->
                            val matchesSearch = item.type.contains(searchQuery, ignoreCase = true)
                            val matchesCategory = if (selectedCategory == "All") true else item.type.equals(selectedCategory, ignoreCase = true)
                            // Customers only see Available items
                            val isAvailable = item.status.lowercase() == "available"

                            matchesSearch && matchesCategory && isAvailable
                        }

                        if (filteredListings.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No products found",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredListings) { item ->
                                    CustomerListingCard(
                                        item = item,
                                        colorIndex = s.listings.indexOf(item),
                                        onBuy = { 
                                            navController.navigate(Screen.CustomerProductDetails.createRoute(item.id.toString())) 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerListingCard(
    item: ProductRequest,
    colorIndex: Int,
    onBuy: () -> Unit
) {
    val bgColor = cardBgColors[colorIndex % cardBgColors.size]
    val emoji = when(item.type.lowercase().take(4)) {
        "eggs" -> "🥚"
        "chic" -> "🐣"
        else -> "🐓"
    }
    
    val unitLabel = when (item.type.lowercase()) {
        "eggs" -> "crate"
        "chicks" -> "bird"
        else -> "bird"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(bgColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 50.sp
                )
            }

            // Info area
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.type.replaceFirstChar { it.uppercaseChar() },
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = "Farm Connect", // Placeholder if farmName isn't strictly loaded
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "₹${"%,.0f".format(item.pricePerUnit)}/$unitLabel",
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${item.quantity}\n${if (unitLabel == "crate") "crates" else "birds"}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onBuy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Buy Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CustomerBottomNavigation(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    if (currentRoute == Screen.CustomerDashboard.route) Icons.Filled.Search else Icons.Outlined.Search, 
                    contentDescription = "Browse"
                ) 
            },
            label = { Text("Browse", fontSize = 11.sp, fontWeight = if (currentRoute == Screen.CustomerDashboard.route) FontWeight.Bold else FontWeight.Normal) },
            selected = currentRoute == Screen.CustomerDashboard.route,
            onClick = {
                if (currentRoute != Screen.CustomerDashboard.route) {
                    navController.navigate(Screen.CustomerDashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color.Transparent
            )
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    if (currentRoute == Screen.CustomerOrders.route) Icons.Filled.ListAlt else Icons.Outlined.ListAlt, 
                    contentDescription = "Orders"
                ) 
            },
            label = { Text("Orders", fontSize = 11.sp, fontWeight = if (currentRoute == Screen.CustomerOrders.route) FontWeight.Bold else FontWeight.Normal) },
            selected = currentRoute == Screen.CustomerOrders.route,
            onClick = {
                if (currentRoute != Screen.CustomerOrders.route) {
                    navController.navigate(Screen.CustomerOrders.route) {
                        popUpTo(Screen.CustomerDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color.Transparent
            )
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    if (currentRoute == Screen.CustomerProfile.route) Icons.Filled.Person else Icons.Outlined.Person, 
                    contentDescription = "Profile"
                ) 
            },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = if (currentRoute == Screen.CustomerProfile.route) FontWeight.Bold else FontWeight.Normal) },
            selected = currentRoute == Screen.CustomerProfile.route,
            onClick = {
                if (currentRoute != Screen.CustomerProfile.route) {
                    navController.navigate(Screen.CustomerProfile.route) {
                        popUpTo(Screen.CustomerDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color.Transparent
            )
        )
    }
}
