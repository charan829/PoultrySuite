package com.simats.poultrysuite.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.Order
import com.simats.poultrysuite.ui.market.MarketViewModel
import com.simats.poultrysuite.ui.market.OrdersState
import com.simats.poultrysuite.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderTrackingScreen(
    navController: NavController,
    orderId: String,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(orderId) {
        viewModel.loadMyOrders()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadMyOrders()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val order = (ordersState as? OrdersState.Success)
        ?.orders
        ?.firstOrNull { it.id == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Order Tracking",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF3F4F6)
    ) { padding ->
        when {
            ordersState is OrdersState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            }

            order == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Order not found", color = Color(0xFF6B7280))
                }
            }

            else -> {
                TrackingContent(
                    order = order,
                    onWriteReview = {
                        navController.navigate(Screen.CustomerWriteReview.createRoute(order.id))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TrackingContent(
    order: Order,
    onWriteReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = order.status.lowercase(Locale.getDefault())
    val isInStore = (order.purchaseType ?: "").equals("IN_STORE", ignoreCase = true) ||
        (order.notes?.contains("in-store", ignoreCase = true) == true) ||
        (order.notes?.contains("in store", ignoreCase = true) == true) ||
        (order.notes?.contains("pickup", ignoreCase = true) == true)

    val confirmedDone = status in setOf("accepted", "confirmed", "dispatched", "completed", "sold", "cancelled", "rejected")
    val stepThreeDone = status in setOf("dispatched", "completed", "sold")
    val deliveredDone = status in setOf("completed", "sold")

    val statusLabel = when {
        status in setOf("completed", "sold") -> if (isInStore) "Picked Up" else "Delivered"
        status == "dispatched" -> if (isInStore) "Ready for Pickup" else "Dispatched"
        status in setOf("accepted", "confirmed") -> "Confirmed"
        status in setOf("cancelled", "rejected") -> "Cancelled"
        else -> "In Progress"
    }

    val statusBg = when (statusLabel) {
        "Delivered", "Picked Up" -> Color(0xFFDCFCE7)
        "Cancelled" -> Color(0xFFFEE2E2)
        else -> Color(0xFFDBEAFE)
    }

    val statusTextColor = when (statusLabel) {
        "Delivered", "Picked Up" -> Color(0xFF166534)
        "Cancelled" -> Color(0xFF991B1B)
        else -> Color(0xFF1D4ED8)
    }

    val deliveryAddress = order.deliveryAddress
        ?.takeIf { it.isNotBlank() }
        ?: order.product?.farm?.location
        ?: "Address unavailable"

    val createdAt = parseIso(order.createdAt)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Order #${order.id}",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "₹${"%,.0f".format(order.totalPrice)}",
                    color = Color(0xFF0F172A),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInStore) Icons.Outlined.Storefront else Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6)
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${order.product?.type?.replaceFirstChar { it.uppercaseChar() } ?: "Product"} x ${order.product?.quantity ?: 0}",
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = order.product?.farm?.name ?: "Farm",
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Order Status",
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                TimelineStep(
                    title = "Order Placed",
                    subtitle = formatTime(createdAt),
                    done = true,
                    showConnector = true
                )
                TimelineStep(
                    title = "Confirmed",
                    subtitle = if (confirmedDone) formatTime(addMinutes(createdAt, 35)) else "Pending",
                    done = confirmedDone,
                    showConnector = true
                )
                TimelineStep(
                    title = if (isInStore) "Ready for Pickup" else "Dispatched",
                    subtitle = if (stepThreeDone) formatTime(addMinutes(createdAt, 95)) else "Pending",
                    done = stepThreeDone,
                    showConnector = true
                )
                TimelineStep(
                    title = if (isInStore) "Picked Up" else "Delivered",
                    subtitle = if (deliveredDone) formatTime(addMinutes(createdAt, 180)) else "Pending",
                    done = deliveredDone,
                    showConnector = false
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isInStore) "Purchase Type" else "Delivery Address",
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (isInStore) {
                    Text(
                        text = "In-store purchase",
                        color = Color(0xFF374151),
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Pickup from ${order.product?.farm?.name ?: "farm store"}",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = deliveryAddress,
                        color = Color(0xFF374151),
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (deliveredDone) "Delivered" else "Estimated delivery: Today, 4:00 PM",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))
                if (order.isReviewed == true) {
                    Text(
                        text = "Review submitted",
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                } else {
                    OutlinedButton(
                        onClick = onWriteReview,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Write a review", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineStep(
    title: String,
    subtitle: String,
    done: Boolean,
    showConnector: Boolean
) {
    val doneBg = Color(0xFF2F855A)
    val pendingBg = Color(0xFFE5E7EB)
    val doneText = Color(0xFF111827)
    val pendingText = Color(0xFF9CA3AF)

    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (done) doneBg else pendingBg),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(width = 2.dp, height = 26.dp)
                        .background(if (done) doneBg else Color(0xFFD1D5DB))
                )
            }
        }

        Spacer(modifier = Modifier.size(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (done) doneText else pendingText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (done) Color(0xFF6B7280) else pendingText
            )
        }
    }
}

private fun parseIso(value: String?): Calendar {
    val calendar = Calendar.getInstance()
    if (value.isNullOrBlank()) return calendar

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss"
    )

    for (pattern in patterns) {
        try {
            val date = SimpleDateFormat(pattern, Locale.getDefault()).parse(value)
            if (date != null) {
                calendar.time = date
                return calendar
            }
        } catch (_: Exception) {
        }
    }

    return calendar
}

private fun addMinutes(base: Calendar, minutes: Int): Calendar {
    return (base.clone() as Calendar).apply { add(Calendar.MINUTE, minutes) }
}

private fun formatTime(calendar: Calendar): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)
}
