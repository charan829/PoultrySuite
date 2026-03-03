package com.simats.poultrysuite.ui.sales

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.AdminSalesStats
import com.simats.poultrysuite.data.model.TransactionItem
import com.simats.poultrysuite.ui.admin.formatCurrency
import com.simats.poultrysuite.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalSalesScreen(
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val salesState by viewModel.salesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSalesData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Total Sales",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Filter logic */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        when (val state = salesState) {
            is SalesState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            }
            is SalesState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = Color.Red)
                }
            }
            is SalesState.Success -> {
                SalesContent(state.stats, padding, navController)
            }
        }
    }
}

@Composable
fun SalesContent(stats: AdminSalesStats, padding: PaddingValues, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // KPI Cards Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KPICard(
                    title = "Total Revenue (Today)",
                    value = formatCurrency(stats.todayRevenue),
                    trend = "+12.5%", // Mock trend
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    title = "Orders (Today)",
                    value = stats.todayOrders.toString(),
                    trend = "+8.2%", // Mock trend
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Revenue Chart
        item {
            WeeklyRevenueChart(stats.weeklyRevenue)
        }

        // Recent Transactions Header
        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Transactions List
        items(stats.recentTransactions) { transaction ->
            TransactionListItem(transaction) {
                navController.navigate(Screen.TransactionDetails.createRoute(transaction.id))
            }
        }
    }
}

@Composable
fun KPICard(title: String, value: String, trend: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trend,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF2E7D32), // Green
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeeklyRevenueChart(data: List<com.simats.poultrysuite.data.model.WeeklyRevenue>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Revenue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "Last 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Bar Chart Canvas
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxRevenue = data.maxOfOrNull { it.revenue } ?: 1.0
                    val barWidth = size.width / (data.size * 1.5f)
                    val space = (size.width - (barWidth * data.size)) / (data.size + 1)
                    val maxHeight = size.height - 30.dp.toPx() // Leave room for labels

                    data.forEachIndexed { index, item ->
                        val barHeight = (item.revenue / maxRevenue) * maxHeight
                        val x = space + (index * (barWidth + space))
                        val y = maxHeight - barHeight

                        // Draw Bar
                        drawRoundRect(
                            color = Color(0xFF388E3C), // Green color matching screenshot
                            topLeft = Offset(x.toFloat(), y.toFloat()),
                            size = Size(barWidth, barHeight.toFloat()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                        
                        // Draw Label (Day)
                        // Note: drawText requires native canvas or text measurer in simpler compose, skipping text for now perfectly or using basic approximation?
                        // For simplicity in Canvas without TextMeasurer (Compose 1.3+), we'll skip labels inside canvas and assume the outer structure handles it or use simple overlay if critical.
                        // Actually, let's just stick to bars.
                    }
                }
                
                // Overlay Labels using Row
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    data.forEach { 
                        Text(text = it.day, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(transaction: TransactionItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.customerName.firstOrNull()?.toString() ?: "C",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.customerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "${transaction.items} • ${transaction.date.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = transaction.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (transaction.status == "Completed" || transaction.status == "SOLD") Color.Gray else Color(0xFFF57F17)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}
