package com.simats.poultrysuite.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.simats.poultrysuite.data.model.AdminStats
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val statsState by viewModel.statsState.collectAsState()
    val stats = (statsState as? AdminState.Success)?.stats

    Scaffold(
        bottomBar = { AdminBottomNavigation(navController) },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        if (statsState is AdminState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF5C6BC0))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Dashboard",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Welcome back, Admin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5C6BC0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    NewStatsGrid(stats, navController)
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    UserGrowthChartCard(stats?.userGrowthData, stats?.userGrowthLabels)
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    RevenueChartCard(stats?.revenueData, stats?.revenueLabels)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun NewStatsGrid(stats: AdminStats?, navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val usersPercent = stats?.usersPercent ?: "+0%"
            val usersNegative = usersPercent.startsWith("-")
            NewStatCard(
                title = "Total Users",
                value = stats?.users?.toString() ?: "0",
                icon = Icons.Filled.Group,
                iconTint = Color(0xFF1565C0), // Blue icon
                iconBg = Color(0xFFE3F2FD), // Light blue padding
                badgeText = usersPercent,
                badgeColor = if (usersNegative) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                badgeBg = if (usersNegative) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                modifier = Modifier.weight(1f).clickable {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.ActiveUsers.route)
                }
            )
            
            val riskPercent = stats?.highRiskPercent ?: "0%"
            val riskNegative = riskPercent.startsWith("-")
            NewStatCard(
                title = "High Risk",
                value = stats?.highRiskUsers?.toString() ?: "0",
                icon = Icons.Filled.Warning,
                iconTint = Color(0xFF1565C0),
                iconBg = Color(0xFFE3F2FD),
                badgeText = riskPercent,
                badgeColor = if (riskNegative) Color(0xFFD32F2F) else Color.Gray,
                badgeBg = if (riskNegative) Color(0xFFFFEBEE) else Color(0xFFF1F5F9),
                modifier = Modifier.weight(1f).clickable {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.ActiveUsers.route)
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val activePercent = stats?.activePercent ?: "+0%"
            val activeNegative = activePercent.startsWith("-")
            NewStatCard(
                title = "Active Now",
                value = stats?.activeNow?.toString() ?: "0",
                icon = Icons.Filled.ShowChart,
                iconTint = Color(0xFF1565C0),
                iconBg = Color(0xFFE3F2FD),
                badgeText = activePercent,
                badgeColor = if (activeNegative) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                badgeBg = if (activeNegative) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                modifier = Modifier.weight(1f).clickable {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.ActiveUsers.route)
                }
            )
            
            val logsPercent = stats?.logsPercent ?: "+0%"
            val logsNegative = logsPercent.startsWith("-")
            NewStatCard(
                title = "Logs Today",
                value = stats?.logsToday ?: "0",
                icon = Icons.Filled.Storage,
                iconTint = Color(0xFF1565C0),
                iconBg = Color(0xFFE3F2FD),
                badgeText = logsPercent,
                badgeColor = if (logsNegative) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                badgeBg = if (logsNegative) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                modifier = Modifier.weight(1f).clickable {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.AdminMarket.route)
                }
            )
        }
    }
}

@Composable
fun NewStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    badgeText: String,
    badgeColor: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier
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
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                
                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = badgeText, color = badgeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun UserGrowthChartCard(data: List<Double>?, labels: List<String>?) {
    val graphLabels = labels ?: listOf("T", "W", "T", "F", "S", "S")
    val graphData = data ?: listOf(0.1, 0.15, 0.3, 0.45, 0.65, 0.85)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "User Growth",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // We assume data is between 0 and 1, or we normalize it
                        val maxVal = graphData.maxOrNull() ?: 1.0
                        val normalizedData = if (maxVal <= 0.0) graphData else graphData.map { it / maxVal }
                        
                        val stepX = width / (normalizedData.size - 1)
                        
                        val path = Path().apply {
                            moveTo(0f, height * (1 - normalizedData[0].toFloat()))
                            
                            for (i in 0 until normalizedData.size - 1) {
                                val x1 = i * stepX
                                val y1 = height * (1 - normalizedData[i].toFloat())
                                val x2 = (i + 1) * stepX
                                val y2 = height * (1 - normalizedData[i + 1].toFloat())
                                
                                val cx = (x1 + x2) / 2
                                cubicTo(cx, y1, cx, y2, x2, y2)
                            }
                        }
                        
                        drawPath(
                            path = path,
                            color = Color(0xFF5C6BC0), // Purple-blue line
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    graphLabels.forEach {
                        Text(text = it, style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueChartCard(data: List<Double>?, labels: List<String>?) {
    val graphData = data ?: listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    val graphLabels = labels ?: listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Revenue Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        val maxVal = graphData.maxOrNull() ?: 1.0
                        val normalizedData = if (maxVal <= 0.0) graphData else graphData.map { it / maxVal }
                        
                        val stepX = width / (normalizedData.size - 1)
                        
                        val path = Path().apply {
                            val startY = if (maxVal <= 0.0) height - 10f else height * (1 - normalizedData[0].toFloat())
                            moveTo(0f, startY)
                            
                            for (i in 0 until normalizedData.size - 1) {
                                val x1 = i * stepX
                                val y1 = if (maxVal <= 0.0) height - 10f else height * (1 - normalizedData[i].toFloat())
                                val x2 = (i + 1) * stepX
                                val y2 = if (maxVal <= 0.0) height - 10f else height * (1 - normalizedData[i + 1].toFloat())
                                
                                val cx = (x1 + x2) / 2
                                cubicTo(cx, y1, cx, y2, x2, y2)
                            }
                        }
                        
                        drawPath(
                            path = path,
                            color = Color(0xFF5C6BC0),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        
                        for (i in normalizedData.indices) {
                            val y = if (maxVal <= 0.0) height - 10f else height * (1 - normalizedData[i].toFloat())
                            drawCircle(
                                color = Color(0xFF5C6BC0),
                                radius = 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(i * stepX, y)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    graphLabels.forEach {
                        Text(text = it, style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val PrimaryPurple = Color(0xFF5C6BC0)
    val UnselectedGray = Color.Gray

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.GridView, contentDescription = "Dash") },
            label = { Text("Dash", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            selected = currentRoute == com.simats.poultrysuite.ui.navigation.Screen.Admin.route,
            onClick = { 
                if (currentRoute != com.simats.poultrysuite.ui.navigation.Screen.Admin.route) {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.Admin.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, 
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = UnselectedGray,
                unselectedTextColor = UnselectedGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Group, contentDescription = "Users") },
            label = { Text("Users", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            selected = currentRoute == com.simats.poultrysuite.ui.navigation.Screen.ActiveUsers.route,
            onClick = { 
                if (currentRoute != com.simats.poultrysuite.ui.navigation.Screen.ActiveUsers.route) {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.ActiveUsers.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, 
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = UnselectedGray,
                unselectedTextColor = UnselectedGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Storage, contentDescription = "Data") },
            label = { Text("Data", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            selected = currentRoute == com.simats.poultrysuite.ui.navigation.Screen.AdminMarket.route,
            onClick = { 
                 if (currentRoute != com.simats.poultrysuite.ui.navigation.Screen.AdminMarket.route) {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.AdminMarket.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, 
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = UnselectedGray,
                unselectedTextColor = UnselectedGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.ListAlt, contentDescription = "Reports") },
            label = { Text("Reports", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            selected = currentRoute == com.simats.poultrysuite.ui.navigation.Screen.AdminReports.route,
            onClick = { 
                  if (currentRoute != com.simats.poultrysuite.ui.navigation.Screen.AdminReports.route) {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.AdminReports.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, 
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = UnselectedGray,
                unselectedTextColor = UnselectedGray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            selected = currentRoute == com.simats.poultrysuite.ui.navigation.Screen.AdminSettings.route,
            onClick = { 
                if (currentRoute != com.simats.poultrysuite.ui.navigation.Screen.AdminSettings.route) {
                    navController.navigate(com.simats.poultrysuite.ui.navigation.Screen.AdminSettings.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple, 
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = UnselectedGray,
                unselectedTextColor = UnselectedGray,
                indicatorColor = Color.Transparent
            )
        )
    }
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount).replace("INR", "₹")
}
