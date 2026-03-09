package com.simats.poultrysuite.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simats.poultrysuite.data.model.SaleRecord
import com.simats.poultrysuite.ui.dashboard.FarmerBottomNavigation
import com.simats.poultrysuite.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Paid", "Pending")

    // Refresh on resume
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Screen.FarmerSales.route) {
            viewModel.loadSales()
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sales",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Screen.FarmerAddSale.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Sale", tint = Color(0xFF1E293B))
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clickable { selectedFilter = filter }
                            .background(
                                if (isSelected) Color(0xFF1565C0) else Color.White,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val s = state) {
                is FarmerSalesState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFF1565C0)) }

                is FarmerSalesState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load sales", color = Color(0xFF94A3B8))
                        TextButton(onClick = { viewModel.loadSales() }) {
                            Text("Retry", color = Color(0xFF1565C0))
                        }
                    }
                }

                is FarmerSalesState.Success -> {
                    val allSales = s.sales
                    val filtered = when (selectedFilter) {
                        "Paid" -> allSales.filter { it.paymentStatus.equals("Paid", ignoreCase = true) }
                        "Pending" -> allSales.filter { it.paymentStatus.equals("Pending", ignoreCase = true) }
                        else -> allSales
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "No ${if (selectedFilter == "All") "" else selectedFilter.lowercase()} sales yet",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { navController.navigate(Screen.FarmerAddSale.route) }) {
                                    Text("Record a sale →", color = Color(0xFF1565C0))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filtered) { sale ->
                                SaleListItem(
                                    sale = sale,
                                    onClick = {
                                        navController.navigate(
                                            Screen.TransactionDetails.createRoute(sale.id)
                                        )
                                    }
                                )
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(horizontal = 24.dp)
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
private fun SaleListItem(sale: SaleRecord, onClick: () -> Unit = {}) {
    val paymentColor = when (sale.paymentStatus.lowercase()) {
        "paid" -> Color(0xFF22C55E)
        "pending" -> Color(0xFFF59E0B)
        "partial" -> Color(0xFF1565C0)
        else -> Color(0xFF94A3B8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sale.buyerName.ifBlank { "Walk-in Customer" },
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${"%,d".format(sale.quantity)} ${sale.productType.lowercase()}s • ${formatSaleDate(sale.createdAt)}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${"%,.0f".format(sale.totalPrice)}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = sale.paymentStatus,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = paymentColor
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun formatSaleDate(isoDate: String): String {
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFmt.timeZone = TimeZone.getTimeZone("UTC")
        val outputFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        outputFmt.format(inputFmt.parse(isoDate) ?: return isoDate)
    } catch (_: Exception) { isoDate }
}
