package com.simats.poultrysuite.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simats.poultrysuite.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.farmState.collectAsState()

    // Refresh every time this screen is at the top of the back-stack (user returned from a sub-screen)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Screen.Dashboard.route) {
            viewModel.loadDashboard()
        }
    }

    Scaffold(
        bottomBar = { FarmerBottomNavigation(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.FarmerAddSale.route) },
                containerColor = Color(0xFF1565C0), // Blue FAB
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Sale", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFF8F9FA) // Very light gray background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp) // Extra bottom padding for FAB
        ) {
            
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good morning,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // Use farm name if available from state, else default
                        val farmName = if (state is DashboardState.Success) {
                            (state as DashboardState.Success).farm.name
                        } else "Adebayo Farms"
                        
                        Text(
                            text = "$farmName 🌾",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .clickable { navController.navigate(Screen.FarmerMessages.route) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Messages",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .clickable { navController.navigate(Screen.FarmerSettings.route) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Grid of 4 KPI Cards
            item {
                val farm = if (state is DashboardState.Success) (state as DashboardState.Success).farm else null

                val totalBirds = farm?.totalBirds ?: 0
                val birdsTrend = farm?.birdsTrend ?: 0.0
                val eggsToday = farm?.eggsToday ?: 0
                val eggsTrend = farm?.eggsTrend ?: 0.0
                val feedRemaining = farm?.feedRemaining ?: 0.0
                val feedTrend = farm?.feedTrend ?: 0.0
                val todayRevenue = farm?.todayRevenue ?: 0.0
                val revenueTrend = farm?.revenueTrend ?: 0.0

                val weeklyData = farm?.weeklyProductionValues ?: List(7) { 0.0 }
                val monthlyData = farm?.monthlyRevenueValues ?: List(6) { 0.0 }

                fun trendStr(t: Double): String {
                    val s = if (t >= 0) "+" else ""
                    return "$s${String.format("%.1f", t)}%"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Pets,
                        iconTint = Color(0xFF1565C0),
                        iconBg = Color(0xFFE3F2FD),
                        trendText = trendStr(birdsTrend),
                        trendUp = birdsTrend >= 0,
                        value = "%,d".format(totalBirds),
                        label = "Total Birds"
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Egg,
                        iconTint = Color(0xFFF57F17),
                        iconBg = Color(0xFFFFF8E1),
                        trendText = trendStr(eggsTrend),
                        trendUp = eggsTrend >= 0,
                        value = "%,d".format(eggsToday),
                        label = "Eggs Today"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Eco,
                        iconTint = Color(0xFF2E7D32),
                        iconBg = Color(0xFFE8F5E9),
                        trendText = trendStr(feedTrend),
                        trendUp = feedTrend >= 0,
                        value = "${String.format("%.0f", feedRemaining)}kg",
                        label = "Feed Remaining"
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFF1565C0),
                        iconBg = Color(0xFFE3F2FD),
                        trendText = trendStr(revenueTrend),
                        trendUp = revenueTrend >= 0,
                        value = "\u20B9${String.format("%,.0f", todayRevenue)}",
                        label = "Today Revenue"
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Weekly Production Chart
            item {
                val weeklyData = if (state is DashboardState.Success)
                    (state as DashboardState.Success).farm.weeklyProductionValues ?: List(7) { 0.0 }
                else List(7) { 0.0 }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Weekly Production",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        WeeklyProductionBarChart(weeklyData)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Monthly Revenue Line Chart
            item {
                val monthlyData = if (state is DashboardState.Success)
                    (state as DashboardState.Success).farm.monthlyRevenueValues ?: List(6) { 0.0 }
                else List(6) { 0.0 }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Monthly Revenue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        MonthlyRevenueLineChart(monthlyData)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    trendText: String,
    trendUp: Boolean,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (trendUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (trendUp) Color(0xFF22C55E) else Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = trendText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trendUp) Color(0xFF22C55E) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun WeeklyProductionBarChart(values: List<Double> = listOf(0.6, 0.5, 0.7, 0.6, 0.8, 0.7, 0.75)) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxValue = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val normalised = values.map { (it / maxValue).toFloat().coerceIn(0.05f, 1f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column {
        // Tooltip row (shown above the selected bar)
        Row(
            modifier = Modifier.fillMaxWidth().height(28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            normalised.forEachIndexed { index, _ ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (selectedIndex == index) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1565C0), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = values[index].toInt().toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            normalised.forEachIndexed { index, value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                        .clickable { selectedIndex = if (selectedIndex == index) null else index }
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(value)
                            .background(
                                if (selectedIndex == index) Color(0xFF0D47A1)
                                else if (index == normalised.size - 1) Color(0xFF1565C0)
                                else Color(0xFFBBD6FB),
                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = days.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedIndex == index) Color(0xFF1565C0) else Color(0xFF94A3B8),
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyRevenueLineChart(values: List<Double> = listOf(0.8, 0.85, 0.9, 1.1, 0.95, 1.05)) {
    val months = listOf("Oct", "Nov", "Dec", "Jan", "Feb", "Mar")
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxVal = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            val points = values.mapIndexed { i, v ->
                val x = if (values.size <= 1) 0f else (i.toFloat() / (values.size - 1).toFloat()) * size.width
                val y = size.height * (1f - (v / maxVal).toFloat()).coerceIn(0.02f, 0.98f)
                Offset(x, y)
            }
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val cp1x = (points[i - 1].x + points[i].x) / 2f
                    cubicTo(cp1x, points[i - 1].y, cp1x, points[i].y, points[i].x, points[i].y)
                }
            }
            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(path = fillPath, color = Color(0xFF1565C0).copy(alpha = 0.1f))
            drawPath(path = path, color = Color(0xFF1565C0), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            points.forEachIndexed { i, point ->
                drawCircle(color = Color.White, radius = 7.dp.toPx(), center = point)
                drawCircle(
                    color = if (selectedIndex == i) Color(0xFF0D47A1) else Color(0xFF1565C0),
                    radius = 7.dp.toPx(), center = point,
                    style = Stroke(width = if (selectedIndex == i) 3.dp.toPx() else 2.dp.toPx())
                )
            }
        }
        // Month labels row
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.takeLast(values.size).forEachIndexed { i, month ->
                Text(
                    text = month,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedIndex == i) Color(0xFF1565C0) else Color(0xFF94A3B8),
                    fontWeight = if (selectedIndex == i) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable { selectedIndex = if (selectedIndex == i) null else i }
                )
            }
        }
        // Tooltip bubble over selected point
        if (selectedIndex != null) {
            val idx = selectedIndex!!
            val x = if (values.size <= 1) 0f else idx.toFloat() / (values.size - 1).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                val yFrac = (1f - (values[idx] / maxVal).toFloat()).coerceIn(0.02f, 0.98f)
                Box(
                    modifier = Modifier
                        .offset(
                            x = (x * 0.85f * 1f).coerceIn(0f, 0.85f).let { frac ->
                                (frac * 300).dp
                            },
                            y = (yFrac * 90).dp
                        )
                        .background(Color(0xFF1565C0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "\u20B9${String.format("%,.0f", values[idx])}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FarmerBottomNavigation(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth() // To match standard bottom nav
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = {
                if (currentRoute != Screen.Dashboard.route) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
            label = { Text("Inventory", fontSize = 10.sp) },
            selected = currentRoute == Screen.FarmerInventory.route,
            onClick = {
                if (currentRoute != Screen.FarmerInventory.route) {
                    navController.navigate(Screen.FarmerInventory.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Sales") },
            label = { Text("Sales", fontSize = 10.sp) },
            selected = currentRoute == Screen.FarmerSales.route,
            onClick = {
                if (currentRoute != Screen.FarmerSales.route) {
                    navController.navigate(Screen.FarmerSales.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Storefront, contentDescription = "Market") },
            label = { Text("Market", fontSize = 10.sp) },
            selected = currentRoute == Screen.Marketplace.route,
            onClick = {
                if (currentRoute != Screen.Marketplace.route) {
                     navController.navigate(Screen.Marketplace.route) {
                         popUpTo(Screen.Dashboard.route) { saveState = true }
                         launchSingleTop = true
                         restoreState = true
                     }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
            label = { Text("Analytics", fontSize = 10.sp) },
            selected = currentRoute == com.simats.poultrysuite.ui.navigation.Screen.Analytics.route,
            onClick = {
                 if (currentRoute != com.simats.poultrysuite.ui.navigation.Screen.Analytics.route) {
                     navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.Analytics.route) {
                         popUpTo(com.simats.poultrysuite.ui.navigation.Screen.Dashboard.route) { saveState = true }
                         launchSingleTop = true
                         restoreState = true
                     }
                 }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1565C0),
                selectedTextColor = Color(0xFF1565C0),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}
