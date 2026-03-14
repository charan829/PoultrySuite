package com.simats.poultrysuite.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.ui.market.MarketViewModel
import com.simats.poultrysuite.ui.market.OrdersState
import com.simats.poultrysuite.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersScreen(
    navController: NavController,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()
    val context = LocalContext.current
    var reviewingOrder by remember { mutableStateOf<com.simats.poultrysuite.data.model.Order?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMyOrders()
    }

    reviewingOrder?.let { order ->
        ReviewDialog(
            farmName = order.product?.farm?.name ?: "the farm",
            onDismiss = { reviewingOrder = null },
            onSubmit = { rating, comment ->
                viewModel.submitReview(
                    orderId = order.id,
                    rating = rating,
                    comment = comment.takeIf { it.isNotBlank() },
                    onSuccess = {
                        Toast.makeText(context, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
                        reviewingOrder = null
                        viewModel.loadMyOrders()
                    },
                    onError = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        reviewingOrder = null
                    }
                )
            }
        )
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
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            items(orders) { order ->
                                val isDelivered = order.status.lowercase().let { it == "completed" || it == "sold" }
                                CustomerOrderCard(
                                    order = order,
                                    onClick = {
                                        navController.navigate(Screen.CustomerOrderTracking.createRoute(order.id))
                                    },
                                    onRateClick = if (isDelivered && order.isReviewed != true) {
                                        { reviewingOrder = order }
                                    } else null
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerOrderCard(
    order: com.simats.poultrysuite.data.model.Order,
    onClick: () -> Unit,
    onRateClick: (() -> Unit)? = null
) {
    val productType = order.product?.type ?: "Unknown"
    val farmName = order.product?.farm?.name ?: "Unknown Farm"

    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = order.createdAt?.let { parser.parse(it) }
        date?.let { formatter.format(it) } ?: "Unknown Date"
    } catch (e: Exception) {
        "Unknown Date"
    }

    val (emoji, bgColor) = when (productType.lowercase().take(4)) {
        "eggs" -> "🥚" to Color(0xFFFEF3C7)
        "chic" -> "🐣" to Color(0xFFFECDD3)
        "poin" -> "🐓" to Color(0xFFDCFCE7)
        else -> "🐔" to Color(0xFFE0E7FF)
    }

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
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (onRateClick != null) 78.dp else 110.dp)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            if (onRateClick != null) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
                TextButton(
                    onClick = onRateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Rate This Farm",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Delivered" -> Color(0xFFDCFCE7) to Color(0xFF166534)
        "Cancelled" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        else -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
