package com.simats.poultrysuite.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.AdminReportsResponse
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    navController: NavController,
    viewModel: AdminReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Reports", 
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1565C0))
                    }
                }
            } else if (state.error != null) {
                item {
                     Text(text = "Error: ${state.error}", color = Color.Red)
                }
            } else if (state.data != null) {
                val data = state.data!!
                
                // 1. Sales Report
                item {
                    SalesReportCard(
                        monthlySales = data.sales.monthly,
                        totalYtd = data.sales.totalYtd
                    )
                }

                // 2. User Growth
                item {
                    UserGrowthCard(
                        monthlyGrowth = data.userGrowth.monthly,
                        totalUsers = data.userGrowth.totalUsers
                    )
                }

                // 3. Marketplace Analytics
                item {
                    MarketplaceAnalyticsCard(
                        activeListings = data.marketplace.activeListings,
                        completedOrders = data.marketplace.completedOrders,
                        avgOrderValue = data.marketplace.avgOrderValue
                    )
                }
            }
        }
    }
}

@Composable
fun SalesReportCard(monthlySales: List<Double>, totalYtd: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sales Report", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("Monthly transaction volume", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            Box(Modifier.height(150.dp).fillMaxWidth()) {
                 if (monthlySales.isNotEmpty()) {
                     LineChart(data = monthlySales)
                 } else {
                     Text("No data", Modifier.align(Alignment.Center))
                 }
            }
            Spacer(modifier = Modifier.height(8.dp))
             Text("Total: " + formatReportCurrency(totalYtd), fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        }
    }
}

@Composable
fun UserGrowthCard(monthlyGrowth: List<Int>, totalUsers: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User Growth", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("New registrations per month", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
             Box(Modifier.height(150.dp).fillMaxWidth()) {
                 Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxGrowth = monthlyGrowth.maxOrNull() ?: 1
                    val scale = if (maxGrowth == 0) 1 else maxGrowth
                    val dataToShow = monthlyGrowth.take(6)
                    dataToShow.forEach { count ->
                        val fraction = (count.toFloat() / scale).coerceAtLeast(0.1f)
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(fraction)
                                .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Users: $totalUsers", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        }
    }
}

@Composable
fun MarketplaceAnalyticsCard(activeListings: Int, completedOrders: Int, avgOrderValue: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Marketplace", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("Listing performance", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Active Listings: $activeListings")
            Text("Completed Orders: $completedOrders")
            Text("Avg Order Value: " + formatReportCurrency(avgOrderValue))
        }
    }
}

@Composable
fun LineChart(data: List<Double>) {
    val chartColor = Color(0xFF1565C0)
    val maxVal = data.maxOrNull() ?: 1.0
    val scale = if (maxVal == 0.0) 1.0 else maxVal
    val displayData = data.take(6) 

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (displayData.size > 1) {
             val spacing = width / (displayData.size - 1)
             val path = Path()
             displayData.forEachIndexed { index, value ->
                 val x = index * spacing
                 val y = height - ((value / scale) * height).toFloat()
                 if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                 drawCircle(chartColor, 4.dp.toPx(), Offset(x, y))
             }
             drawPath(path, chartColor, style = Stroke(3.dp.toPx()))
        }
    }
}

private fun formatReportCurrency(amount: Double): String {
    return "₦" + NumberFormat.getNumberInstance(Locale.US).format(amount)
}
