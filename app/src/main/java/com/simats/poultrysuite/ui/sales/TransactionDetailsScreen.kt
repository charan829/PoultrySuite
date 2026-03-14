package com.simats.poultrysuite.ui.sales

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.OrderDetail
import com.simats.poultrysuite.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    navController: NavController,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var isOpeningChat by remember { mutableStateOf(false) }
    var isMarkingComplete by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        viewModel.loadOrder(transactionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        when (val s = state) {
            is OrderDetailState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFF1565C0)) }

            is OrderDetailState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = Color(0xFF94A3B8))
                    TextButton({ viewModel.loadOrder(transactionId) }) {
                        Text("Retry", color = Color(0xFF1565C0))
                    }
                }
            }

            is OrderDetailState.Success -> OrderDetailsContent(
                order = s.order,
                padding = padding,
                onMarkPaid = { viewModel.markAsPaid(transactionId) },
                isMarkingComplete = isMarkingComplete,
                onMarkComplete = {
                    if (!isMarkingComplete) {
                        isMarkingComplete = true
                        viewModel.markAsComplete(
                            id = transactionId,
                            onSuccess = {
                                isMarkingComplete = false
                                Toast.makeText(context, "Order marked as complete", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                isMarkingComplete = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                isOpeningChat = isOpeningChat,
                onMessageCustomer = {
                    if (isOpeningChat) return@OrderDetailsContent
                    isOpeningChat = true
                    viewModel.startConversationFromOrder(
                        orderId = transactionId,
                        onResult = { conversationId, partnerName ->
                            isOpeningChat = false
                            navController.navigate(Screen.FarmerChat.createRoute(conversationId, partnerName)) {
                                launchSingleTop = true
                            }
                        },
                        onError = { error ->
                            isOpeningChat = false
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun OrderDetailsContent(
    order: OrderDetail,
    padding: PaddingValues,
    onMarkPaid: () -> Unit,
    isMarkingComplete: Boolean = false,
    onMarkComplete: () -> Unit = {},
    isOpeningChat: Boolean,
    onMessageCustomer: () -> Unit
) {
    val isPaid = order.paymentStatus.equals("Paid", ignoreCase = true)
    val isComplete = order.status.lowercase(java.util.Locale.getDefault()).let { it == "completed" || it == "sold" }
    val statusColor = when (order.paymentStatus.lowercase()) {
        "paid" -> Color(0xFF22C55E)
        "pending" -> Color(0xFFF59E0B)
        else -> Color(0xFF1565C0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ──── Order Summary Card ────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Order #${order.id}",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        order.paymentStatus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "₹${"%,.0f".format(order.totalPrice)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(Modifier.height(16.dp))

                // Product line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${order.quantity} ${order.productType.lowercase().replaceFirstChar { it.uppercaseChar() }}s",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        "₹${"%,.0f".format(order.pricePerUnit)} each",
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
                    Text(
                        "₹${"%,.0f".format(order.totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }

        // ──── Customer Information ──────────────────────────────────
        SectionCard(
            title = "Customer Information",
            headerAction = {
                FilledTonalButton(
                    onClick = onMessageCustomer,
                    enabled = !isOpeningChat,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF1565C0)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isOpeningChat) "Opening..." else "Message",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        ) {
            CustomerRow(
                icon = Icons.Default.Person,
                primaryText = order.buyerName.ifBlank { "Walk-in Customer" },
                secondaryText = order.buyerType
            )
            if (!order.buyerPhone.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                CustomerRow(
                    icon = Icons.Default.Phone,
                    primaryText = order.buyerPhone,
                    secondaryText = null
                )
            }
            if (!order.buyerAddress.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                CustomerRow(
                    icon = Icons.Default.LocationOn,
                    primaryText = order.buyerAddress,
                    secondaryText = null
                )
            }
            if (!order.notes.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                CustomerRow(
                    icon = Icons.Default.Notes,
                    primaryText = order.notes,
                    secondaryText = null
                )
            }
        }

        // ──── Payment Status ────────────────────────────────────────
        SectionCard(title = "Payment Status") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CreditCard, null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(order.paymentMethod, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        Text(
                            if (isPaid) "Payment received" else "Awaiting payment",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                Text(
                    order.paymentStatus,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Due: ${formatOrderDate(order.dueDate ?: order.createdAt)}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ──── Action Buttons ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO: send reminder via SMS/notification */ },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
            ) {
                Text("Send Reminder", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onMarkPaid,
                modifier = Modifier.weight(1f).height(50.dp),
                enabled = !isPaid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    disabledContainerColor = Color(0xFF94A3B8)
                )
            ) {
                Text(if (isPaid) "Already Paid" else "Mark as Paid", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        // ──── Mark as Complete ───────────────────────────────────────
        Button(
            onClick = onMarkComplete,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !isComplete && !isMarkingComplete,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1565C0),
                disabledContainerColor = Color(0xFF94A3B8)
            )
        ) {
            if (isMarkingComplete) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (isComplete) "Order Completed" else "Mark as Complete",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    headerAction: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = 15.sp
                )
                headerAction?.invoke(this)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun CustomerRow(icon: ImageVector, primaryText: String, secondaryText: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(primaryText, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), fontSize = 14.sp)
            if (secondaryText != null) {
                Text(secondaryText, fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

private fun formatOrderDate(iso: String): String {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(fmt.parse(iso) ?: return iso)
    } catch (_: Exception) { iso }
}
