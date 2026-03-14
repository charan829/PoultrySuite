package com.simats.poultrysuite.ui.customer

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.ui.market.MarketViewModel
import com.simats.poultrysuite.ui.market.OrdersState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomerWriteReviewScreen(
    navController: NavController,
    orderId: String,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()
    val context = LocalContext.current

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        if (ordersState !is OrdersState.Success) {
            viewModel.loadMyOrders()
        }
    }

    val order = (ordersState as? OrdersState.Success)
        ?.orders
        ?.firstOrNull { it.id == orderId }

    val farmName = order?.product?.farm?.name ?: "the farm"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Write a Review",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Rate your experience",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "How was your order from $farmName?",
                                textAlign = TextAlign.Center,
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..5) {
                                    IconButton(onClick = { rating = i }) {
                                        Icon(
                                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = "Rating $i",
                                            tint = if (i <= rating) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Tell us more about your experience",
                                color = Color(0xFF1E3A8A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = comment,
                                onValueChange = { comment = it },
                                placeholder = {
                                    Text(
                                        "Were the products fresh? Was delivery on time? Would you buy again?",
                                        color = Color(0xFF9CA3AF)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(124.dp),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD1D5DB),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (rating <= 0) return@Button
                            isSubmitting = true
                            viewModel.submitReview(
                                orderId = order.id,
                                rating = rating,
                                comment = comment.takeIf { it.isNotBlank() },
                                onSuccess = {
                                    isSubmitting = false
                                    Toast.makeText(context, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
                                    viewModel.loadMyOrders()
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("reviewSubmitted", true)
                                    navController.popBackStack()
                                },
                                onError = { message ->
                                    isSubmitting = false
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = rating > 0 && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0),
                            disabledContainerColor = Color(0xFFD1D5DB)
                        )
                    ) {
                        Text(
                            text = if (isSubmitting) "Submitting..." else "Submit Review",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
