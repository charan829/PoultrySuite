package com.simats.poultrysuite.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import com.simats.poultrysuite.ui.market.MarketViewModel
import com.simats.poultrysuite.ui.market.OrdersState
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersScreen(
    navController: NavController,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text("My Orders", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1E293B))
                        
                        val orderCount = if (ordersState is OrdersState.Success) {
                            (ordersState as OrdersState.Success).orders.size
                        } else 0
                        
                        Text("$orderCount orders", fontSize = 14.sp, color = Color(0xFF64748B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.height(100.dp)
            )
        },
        bottomBar = { CustomerBottomNavigation(navController) },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (ordersState) {
                is OrdersState.Loading -> {
                    CircularProgressIndicator(
                        color = Color(0xFF1565C0),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OrdersState.Error -> {
                    Text(
                        text = (ordersState as OrdersState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OrdersState.Success -> {
                    val orders = (ordersState as OrdersState.Success).orders
                    if (orders.isEmpty()) {
                        Text(
                            text = "No orders found.",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp) // creates a subtle separator effect
                        ) {
                            items(orders) { order ->
                                CustomerOrderCard(order = order)
                                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerOrderCard(order: com.simats.poultrysuite.data.model.Order) {
    val productType = order.product?.type ?: "Unknown"
    val farmName = order.product?.farm?.name ?: "Unknown Farm"
    
    // Format Date (e.g., 10 Feb 2024)
    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = parser.parse(order.createdAt)
        date?.let { formatter.format(it) } ?: "Unknown Date"
    } catch (e: Exception) {
        "Unknown Date"
    }

    // Determine Emoji and Background based on type
    val (emoji, bgColor) = when (productType.lowercase().take(4)) {
        "eggs" -> "🥚" to Color(0xFFFEF3C7) // Pastel Yellow
        "chic" -> "🐣" to Color(0xFFFECDD3) // Pastel Pink/Red
        "poin" -> "🐓" to Color(0xFFDCFCE7) // Pastel Green
        else -> "🐔" to Color(0xFFE0E7FF)   // Pastel Blue
    }

    // Map Backend Status to UI Status
    val uiStatus = when (order.status.lowercase()) {
        "pending" -> "In Progress"
        "completed" -> "Delivered"
        "sold" -> "Delivered"
        "cancelled" -> "Cancelled"
        else -> order.status
    }
    
    val unitLabel = if (productType.lowercase() == "eggs") "crates" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp) // Flush like a list item
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${productType.replaceFirstChar { it.uppercaseChar() }} × ${order.product?.quantity ?: 0} $unitLabel".trim(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$farmName • $formattedDate",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price & Status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹${"%,.0f".format(order.totalPrice)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(status = uiStatus)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "in progress" -> Color(0xFFEFF6FF) to Color(0xFF2563EB) // Blue
        "delivered" -> Color(0xFFF0FDF4) to Color(0xFF16A34A)   // Green
        "cancelled" -> Color(0xFFFEF2F2) to Color(0xFFDC2626)   // Red
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B)          // Gray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
