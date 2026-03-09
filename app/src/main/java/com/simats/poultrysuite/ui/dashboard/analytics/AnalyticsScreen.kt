package com.simats.poultrysuite.ui.dashboard.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.AnalyticsResponse
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                actions = {
                    TextButton(onClick = { navController.navigate("add_expense") }) {
                        Text("+ Expense", color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        bottomBar = { com.simats.poultrysuite.ui.dashboard.FarmerBottomNavigation(navController) },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val s = state) {
                is AnalyticsState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is AnalyticsState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Color.Red)
                        Button(onClick = { viewModel.loadAnalytics() }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Retry")
                        }
                    }
                }
                is AnalyticsState.Success -> {
                    val data = s.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ThisMonthCard(data.revenue, data.expenses, data.netProfit)
                        }
                        item {
                            ExpenseBreakdownCard(data.expenseBreakdown, data.expenses)
                        }
                        item {
                            RevenueTrendCard(data.revenueTrend)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThisMonthCard(revenue: Double, expenses: Double, netProfit: Double) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("This Month", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            
            // Revenue
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Revenue", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Revenue", color = Color(0xFF64748B), fontSize = 14.sp)
                }
                Text(currencyFormat.format(revenue), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }

            // Expenses
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.TrendingDown, contentDescription = "Expenses", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Expenses", color = Color(0xFF64748B), fontSize = 14.sp)
                }
                Text(currencyFormat.format(expenses), fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            }
            
            Divider(color = Color(0xFFF1F5F9))

            // Net Profit
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Net Profit", tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Net Profit", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text(currencyFormat.format(netProfit), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1565C0))
            }
        }
    }
}

@Composable
fun ExpenseBreakdownCard(breakdown: List<com.simats.poultrysuite.data.model.ExpenseBreakdownItem>, totalExpenses: Double) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val colors = listOf(Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFFF57C00), Color(0xFF7B1FA2), Color(0xFF607D8B))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Expense Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            
            val orderedBreakdown = breakdown.sortedByDescending { it.amount }
            
            orderedBreakdown.forEachIndexed { index, item ->
                val fraction = if (totalExpenses > 0) (item.amount / totalExpenses).toFloat() else 0f
                val color = colors.getOrElse(index) { Color.Gray }
                
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.category, color = Color(0xFF64748B), fontSize = 14.sp)
                        Text(currencyFormat.format(item.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFF1F5F9), CircleShape)) {
                        Box(modifier = Modifier.fillMaxWidth(fraction).height(6.dp).background(color, CircleShape))
                    }
                }
            }
            if (orderedBreakdown.isEmpty()) {
                Text("No expenses recorded yet.", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RevenueTrendCard(trendPoints: List<Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Revenue Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                if (trendPoints.all { it == 0.0 }) {
                   Text("Not enough data to graph", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                } else {
                   val maxPoint = trendPoints.maxOrNull() ?: 1.0
                   val normalizedPoints = trendPoints.map { if (maxPoint > 0) (it / maxPoint).toFloat() else 0f }
                   
                   Canvas(modifier = Modifier.fillMaxSize()) {
                       val canvasWidth = size.width
                       val canvasHeight = size.height
                       val stepX = canvasWidth / (normalizedPoints.size - 1).coerceAtLeast(1)
                       
                       val path = Path()
                       var lastX = 0f
                       var lastY = 0f
                       
                       normalizedPoints.forEachIndexed { index, value ->
                           val x = index * stepX
                           val y = canvasHeight - (value * canvasHeight)
                           
                           if (index == 0) {
                               path.moveTo(x, y)
                           } else {
                               path.lineTo(x, y)
                           }
                           lastX = x
                           lastY = y
                       }
                       
                       // Draw Line
                       drawPath(
                           path = path,
                           color = Color(0xFF4CAF50),
                           style = Stroke(width = 4f)
                       )
                       
                       // Draw Points
                       normalizedPoints.forEachIndexed { index, value ->
                           val x = index * stepX
                           val y = canvasHeight - (value * canvasHeight)
                           drawCircle(
                               color = Color.White,
                               radius = 8f,
                               center = Offset(x, y)
                           )
                           drawCircle(
                               color = Color(0xFF2E7D32),
                               radius = 8f,
                               center = Offset(x, y),
                               style = Stroke(width = 3f)
                           )
                       }
                       
                       // Fill area under line
                       val fillPath = Path()
                       fillPath.addPath(path)
                       fillPath.lineTo(canvasWidth, canvasHeight)
                       fillPath.lineTo(0f, canvasHeight)
                       fillPath.close()
                       
                       drawPath(
                           path = fillPath,
                           color = Color(0xFFE8F5E9).copy(alpha = 0.5f)
                       )
                   }
                }
            }
        }
    }
}
