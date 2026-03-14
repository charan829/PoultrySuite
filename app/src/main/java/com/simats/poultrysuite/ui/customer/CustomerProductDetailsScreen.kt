package com.simats.poultrysuite.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
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
import com.simats.poultrysuite.data.model.Review
import com.simats.poultrysuite.ui.market.MarketState
import com.simats.poultrysuite.ui.market.MarketViewModel
import com.simats.poultrysuite.ui.market.ReviewsState
import com.simats.poultrysuite.ui.navigation.Screen
import com.simats.poultrysuite.ui.navigation.Screen.CustomerChat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProductDetailsScreen(
    navController: NavController,
    productId: String,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val state by viewModel.marketState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val canReview by viewModel.canReview.collectAsState()
    val context = LocalContext.current
    val messagingViewModel: MessagingViewModel = hiltViewModel()

    val item = if (state is MarketState.Success) {
        (state as MarketState.Success).listings.find { it.id.toString() == productId }
    } else {
        null
    }

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1565C0))
        }
        return
    }

    var showReviewDialog by remember(item.id) { mutableStateOf(false) }
    var showOrderDialog by remember(item.id) { mutableStateOf(false) }

    LaunchedEffect(item.farmId) {
        viewModel.loadFarmReviews(item.farmId)
        viewModel.checkCanReview(item.farmId)
    }

    val reviews = when (reviewsState) {
        is ReviewsState.Success -> (reviewsState as ReviewsState.Success).reviews
        else -> emptyList()
    }
    val averageRating = reviews.takeIf { it.isNotEmpty() }?.map { it.rating }?.average()
    val reviewCountLabel = if (reviews.size == 1) "1 review" else "${reviews.size} reviews"

    val emoji = when (item.type.lowercase().take(4)) {
        "eggs" -> "🥚"
        "chic" -> "🐣"
        else -> "🐓"
    }

    val unitLabel = when (item.type.lowercase()) {
        "eggs" -> "crate"
        "chicks" -> "bird"
        else -> "bird"
    }

    val descriptionText = "Healthy, well-fed ${item.type.lowercase()} raised in a clean environment. " +
        "Fed with quality feed and properly vaccinated. Ready for immediate pickup or delivery."

    if (showReviewDialog && canReview?.orderId != null) {
        ReviewDialog(
            farmName = item.farm?.name ?: "this farm",
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitReview(
                    orderId = canReview?.orderId.orEmpty(),
                    rating = rating,
                    comment = comment.takeIf { it.isNotBlank() },
                    onSuccess = {
                        Toast.makeText(context, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
                        showReviewDialog = false
                        viewModel.loadFarmReviews(item.farmId)
                        viewModel.checkCanReview(item.farmId)
                        viewModel.loadMyOrders()
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showOrderDialog) {
        OrderOptionsDialog(
            farmName = item.farm?.name ?: "this farm",
            availableQuantity = item.quantity,
            onDismiss = { showOrderDialog = false },
            onConfirm = { purchaseType, deliveryAddress, quantity ->
                viewModel.placeOrder(
                    productId = item.id.toString(),
                    purchaseType = purchaseType,
                    quantity = quantity,
                    deliveryAddress = deliveryAddress,
                    onSuccess = {
                        showOrderDialog = false
                        navController.navigate(Screen.CustomerOrderSuccess.route)
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        showOrderDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("Buy Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 100.sp)
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fresh ${item.type.replaceFirstChar { it.uppercaseChar() }}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 22.sp
                    )

                    if (item.status.lowercase() == "available") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "In Stock",
                                color = Color(0xFF166534),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${"%,.0f".format(item.pricePerUnit)}",
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "/$unitLabel",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(Screen.CustomerFarmProfile.createRoute(item.farmId))
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌾", fontSize = 24.sp)
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.farm?.name ?: "Unknown Farm",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = item.farm?.location ?: "Location not specified",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = if (averageRating != null) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = averageRating?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "New",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (reviews.isEmpty()) "No reviews" else reviewCountLabel,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Message Farm button
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        messagingViewModel.startConversationAndOpen(
                            farmId = item.farmId,
                            onResult = { convoId, farmNameResult ->
                                navController.navigate(Screen.CustomerChat.createRoute(convoId, farmNameResult))
                            },
                            onError = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1565C0))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Message Farm", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = descriptionText,
                            color = Color(0xFF64748B),
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Customer Reviews",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                if (averageRating != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ReviewStars(rating = averageRating.roundToInt())
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", averageRating)} • $reviewCountLabel",
                                            color = Color(0xFF64748B),
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No customer reviews yet.",
                                        color = Color(0xFF64748B),
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (canReview?.canReview == true && canReview?.orderId != null) {
                                OutlinedButton(
                                    onClick = { showReviewDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Write Review")
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        when (reviewsState) {
                            is ReviewsState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF1565C0),
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                            is ReviewsState.Error -> {
                                Text(
                                    text = (reviewsState as ReviewsState.Error).message,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 13.sp
                                )
                            }
                            is ReviewsState.Success -> {
                                if (reviews.isEmpty()) {
                                    Text(
                                        text = "Reviews will appear here after customers complete their orders.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                } else {
                                    reviews.forEachIndexed { index, review ->
                                        ReviewCard(review = review)
                                        if (index != reviews.lastIndex) {
                                            Spacer(Modifier.height(12.dp))
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            Spacer(Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                            ReviewsState.Idle -> Unit
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ReviewStars(rating: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (index in 1..5) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (index <= rating) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                modifier = Modifier.size(14.dp)
            )
            if (index != 5) {
                Spacer(Modifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.customerName,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(6.dp))
                ReviewStars(rating = review.rating)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = formatReviewDate(review.createdAt),
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        if (!review.comment.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = review.comment,
                color = Color(0xFF475569),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

private fun formatReviewDate(dateString: String): String {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX"
    )
    val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    patterns.forEach { pattern ->
        try {
            val input = SimpleDateFormat(pattern, Locale.getDefault())
            val date = input.parse(dateString)
            if (date != null) {
                return output.format(date)
            }
        } catch (_: Exception) {
        }
    }
    return dateString.take(10)
}

@Composable
private fun OrderOptionsDialog(
    farmName: String,
    availableQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: (purchaseType: String, deliveryAddress: String?, quantity: Int) -> Unit
) {
    var purchaseType by remember { mutableStateOf("ONLINE") }
    var deliveryAddress by remember { mutableStateOf("") }
    var quantity by remember(availableQuantity) { mutableStateOf(1) }
    val requiresAddress = purchaseType == "ONLINE"
    val hasStock = availableQuantity > 0
    val canConfirm = hasStock && (!requiresAddress || deliveryAddress.isNotBlank())

    LaunchedEffect(availableQuantity) {
        if (availableQuantity <= 0) {
            quantity = 0
        } else {
            if (quantity < 1) quantity = 1
            if (quantity > availableQuantity) quantity = availableQuantity
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        purchaseType,
                        deliveryAddress.trim().takeIf { it.isNotBlank() },
                        quantity
                    )
                },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        },
        title = {
            Text(
                text = "Choose delivery option",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "How should ${farmName} fulfill this order?",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Quantity",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Available: $availableQuantity",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        if (hasStock) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { if (quantity > 1) quantity -= 1 },
                                    enabled = quantity > 1
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = quantity.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(Modifier.width(12.dp))
                                FilledTonalIconButton(
                                    onClick = { if (quantity < availableQuantity) quantity += 1 },
                                    enabled = quantity < availableQuantity
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                                }
                            }
                        } else {
                            Text(
                                text = "Out of stock",
                                fontSize = 13.sp,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (purchaseType == "ONLINE") Color(0xFFEFF6FF) else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (purchaseType == "ONLINE") Color(0xFF1565C0) else Color(0xFFE5E7EB)
                    ),
                    onClick = { purchaseType = "ONLINE" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = purchaseType == "ONLINE", onClick = { purchaseType = "ONLINE" })
                        Column {
                            Text("Online delivery", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            Text("Provide delivery address", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (purchaseType == "IN_STORE") Color(0xFFF8FAFC) else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (purchaseType == "IN_STORE") Color(0xFF1565C0) else Color(0xFFE5E7EB)
                    ),
                    onClick = { purchaseType = "IN_STORE" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = purchaseType == "IN_STORE", onClick = { purchaseType = "IN_STORE" })
                        Column {
                            Text("In-store purchase", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            Text("Pick up from the farm store", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                if (requiresAddress) {
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Delivery address") },
                        placeholder = { Text("15 Market Road, Bodija, Ibadan") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1565C0),
                            focusedLabelColor = Color(0xFF1565C0)
                        )
                    )
                }
            }
        }
    )
}

