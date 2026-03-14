package com.simats.poultrysuite.ui.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.simats.poultrysuite.ui.dashboard.FarmerBottomNavigation
import com.simats.poultrysuite.ui.navigation.Screen

// Pastel background colors for listing cards (cycles per index)
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
fun MarketScreen(
    navController: NavController,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val state by viewModel.marketState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    // Refresh on resume
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == "marketplace") {
            viewModel.loadListings()
        }
    }

    Scaffold(
        bottomBar = { FarmerBottomNavigation(navController) },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Listings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                if (userRole == "FARMER") {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable { navController.navigate(Screen.FarmerCreateListing.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Listing", tint = Color(0xFF1E293B))
                    }
                }
            }

            // ── Content ─────────────────────────────────────────────
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
                    if (s.listings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "No listings yet",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                TextButton({ navController.navigate(Screen.FarmerCreateListing.route) }) {
                                    Text("Create your first listing →", color = Color(0xFF1565C0))
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.listings) { item ->
                                ListingGridCard(
                                    item = item,
                                    colorIndex = s.listings.indexOf(item),
                                    isCustomer = userRole == "CUSTOMER",
                                    onBuy = { 
                                        viewModel.placeOrder(
                                            productId = item.id,
                                            onSuccess = {
                                            navController.navigate(Screen.CustomerOrderSuccess.route)
                                            }
                                        )
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

@Composable
fun ListingGridCard(
    item: ProductRequest,
    colorIndex: Int,
    isCustomer: Boolean,
    onBuy: () -> Unit
) {
    val bgColor = cardBgColors[colorIndex % cardBgColors.size]
    val statusColor = when (item.status.lowercase()) {
        "pending" -> Color(0xFFF59E0B)
        "sold" -> Color(0xFFEF4444)
        else -> Color(0xFF22C55E)
    }
    val statusLabel = when (item.status.lowercase()) {
        "available" -> "Active"
        "pending" -> "Pending"
        "sold" -> "Sold"
        else -> item.status.replaceFirstChar { it.uppercaseChar() }
    }
    val unitLabel = when (item.type.lowercase()) {
        "eggs" -> "crate"
        "chicks" -> "chick"
        else -> "bird"
    }
    val qtyLabel = "${item.quantity} ${item.type.lowercase()} available"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // Image area with pastel bg + egg emoji
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(bgColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.type.lowercase()) {
                        "eggs" -> "🥚"
                        "chicks" -> "🐣"
                        else -> "🐓"
                    },
                    fontSize = 48.sp
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.type.replaceFirstChar { it.uppercaseChar() },
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = 14.sp
                )
                Text(
                    text = qtyLabel,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹${"%,.0f".format(item.pricePerUnit)}/$unitLabel",
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(6.dp))
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (isCustomer && item.status.lowercase() == "available") {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onBuy,
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Buy", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// Keep this as a simple utility composable - ListingCard (used externally)
@Composable
fun ListingCard(item: ProductRequest, isCustomer: Boolean, onBuy: () -> Unit) {
    ListingGridCard(item = item, colorIndex = 0, isCustomer = isCustomer, onBuy = onBuy)
}

// Keep AddListingDialog stub
@Composable
fun AddListingDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    onDismiss()
}
