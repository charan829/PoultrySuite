package com.simats.poultrysuite.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.poultrysuite.ui.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMarketScreen(
    navController: NavController,
    viewModel: AdminMarketViewModel = hiltViewModel()
) {
    val marketState by viewModel.marketState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Marketplace Activity", 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
             com.simats.poultrysuite.ui.admin.AdminBottomNavigation(navController)
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        
        when (val state = marketState) {
            is MarketState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            }
            is MarketState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                }
            }
            is MarketState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                     contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 1. Transaction Volume Graph
                    item {
                        TransactionVolumeCard(weeklyRevenue = state.salesStats.weeklyRevenue)
                    }

                    // 2. Flagged Items (Only show if not empty)
                    if (state.flaggedItems.isNotEmpty()) {
                        item {
                            FlaggedItemsSection(flaggedItems = state.flaggedItems)
                        }
                    } else {
                        item {
                           // Optional: Message saying no flagged items, or just hide it
                            Text(
                                "No flagged items to review",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // 3. Recent Listings
                    item {
                        RecentListingsSection(listings = state.recentListings)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionVolumeCard(weeklyRevenue: List<com.simats.poultrysuite.data.model.WeeklyRevenue>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Transaction Volume (This Week)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (weeklyRevenue.isEmpty()) {
                Text("No data available", color = Color.Gray)
            } else {
                // Find max revenue for scaling
                val maxRevenue = weeklyRevenue.maxOfOrNull { it.revenue } ?: 1.0
                val scale = if (maxRevenue == 0.0) 1.0 else maxRevenue

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyRevenue.forEach { dayData ->
                        val fraction = (dayData.revenue / scale).toFloat().coerceIn(0.1f, 1f) // Ensure at least some height
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                             modifier = Modifier.fillMaxHeight().weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .fillMaxHeight(fraction)
                                    .background(Color(0xFF1565C0), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                dayData.day.take(3), 
                                style = MaterialTheme.typography.labelSmall, 
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlaggedItemsSection(flaggedItems: List<com.simats.poultrysuite.data.model.FlaggedItem>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Flagged Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                "Needs Review",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFC62828),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
             colors = CardDefaults.cardColors(containerColor = Color.White),
             shape = RoundedCornerShape(12.dp),
             modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                flaggedItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Flagged", tint = Color(0xFFC62828))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.itemName, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(item.reason, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₦${item.price}", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(item.farmName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    if (index < flaggedItems.size - 1) {
                         Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentListingsSection(listings: List<com.simats.poultrysuite.data.model.ProductRequest>) {
    Column {
        Text(
            "Recent Listings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        if (listings.isEmpty()) {
             Text(
                "No listings available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            Card(
                 colors = CardDefaults.cardColors(containerColor = Color.White),
                 shape = RoundedCornerShape(12.dp),
                 modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    listings.forEachIndexed { index, listing ->
                        // Need farm name in ProductRequest or separate fetch?
                        // Schema says ProductRequest includes farm relation.
                        // Assuming fetch includes farm.
                        val farmName = listing.farm?.name ?: "Unknown Farm"
                        
                        RecentListingItem(
                            title = "${listing.quantity} ${listing.type}",
                            farm = farmName,
                            price = "₦${listing.pricePerUnit}",
                            status = listing.status,
                            isLast = index == listings.size - 1
                        )
                         if (index < listings.size - 1) {
                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentListingItem(title: String, farm: String, price: String, status: String, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
         Column {
            Text(title, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
            Text(farm, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(price, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            
            val (bgColor, textColor) = when(status) {
                "Active" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                "Pending" -> Color(0xFFFFF3E0) to Color(0xFFF57F17)
                else -> Color.LightGray to Color.White
            }
             Surface(
                 color = bgColor,
                 shape = RoundedCornerShape(4.dp)
            ) {
                 Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
