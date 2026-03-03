package com.simats.poultrysuite.ui.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.simats.poultrysuite.data.model.UserDetails
import com.simats.poultrysuite.ui.admin.formatCurrency
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    userId: String,
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val userState by viewModel.userDetailsState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUserDetails(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "User Profile", 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(end = 48.dp) // Offset for back button to center exactly
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        when (val state = userState) {
            is UserDetailsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5C6BC0))
                }
            }
            is UserDetailsState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = Color.Red)
                }
            }
            is UserDetailsState.Success -> {
                UserDetailsContent(state.details, padding)
            }
        }
    }
}

@Composable
fun UserDetailsContent(user: UserDetails, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            val initials = user.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
            Text(
                text = initials,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Name and Email
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF94A3B8)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Status Badge
        val statusBg = if (user.status == "SUSPENDED") Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
        val statusTextCol = if (user.status == "SUSPENDED") Color(0xFFEF4444) else Color(0xFF22C55E)
        val statusText = if (user.status == "SUSPENDED") "Suspended User" else "Active User"
        
        Surface(
            color = statusBg,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = statusTextCol,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // KPI Row (ORDERS | RISK | DAYS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KpiCard(
                label = "ORDERS",
                value = user.totalOrders.toString(),
                valueColor = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )
            
            val riskColor = try {
                Color(android.graphics.Color.parseColor(user.riskColorHex ?: "#22C55E"))
            } catch (e: Exception) {
                Color(0xFF22C55E)
            }
            
            KpiCard(
                label = "RISK",
                value = user.riskText,
                valueColor = riskColor,
                modifier = Modifier.weight(1f)
            )
            
            KpiCard(
                label = "DAYS",
                value = user.daysActive.toString(),
                valueColor = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Risk History
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Risk History (7 Days)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                     ActivityGraph(user.activityGraph)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // X-Axis
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    days.forEach { 
                        Text(text = it, style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Recent Activity
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (user.recentActivity.isEmpty()) {
             Text(
                 text = "No recent activity",
                 style = MaterialTheme.typography.bodyMedium,
                 color = Color(0xFF94A3B8)
             )
        } else {
            user.recentActivity.forEach { activity ->
                 val icon = when(activity.type) {
                     "ORDER" -> Icons.Default.ShoppingCart
                     "LISTING" -> Icons.Default.ShoppingCart // Safe fallback
                     "ALERT" -> Icons.Default.Warning
                     else -> Icons.Default.Today
                 }
                 val timeStr = formatRelativeTimeStr(activity.time)
                 ActivityItem(
                     icon = icon,
                     title = activity.title,
                     subtitle = activity.subtitle,
                     time = timeStr,
                     isAlert = activity.type == "ALERT"
                 )
                 Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        OutlinedButton(
            onClick = { /* Block User Action */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF97316)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316))
        ) {
            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Block User", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* Delete Account Action */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun KpiCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun ActivityItem(icon: ImageVector, title: String, subtitle: String, time: String, isAlert: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if (isAlert) Color(0xFF94A3B8) else Color(0xFF94A3B8), // Using gray for icons based on mockup
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = subtitle, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color(0xFF94A3B8)
                )
            }
            Text(
                text = time, 
                style = MaterialTheme.typography.labelMedium, 
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun ContactRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
    }
}

@Composable
fun StatBox(label: String, value: String, icon: ImageVector, iconBg: Color, iconTint: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun ActivityGraph(data: List<Int>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val maxVal = data.maxOrNull()?.toFloat() ?: 1f
        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * (if (data.size > 1) width / (data.size - 1) else width),
                y = height - (value / maxVal * height * 0.8f) // 0.8 to leave some top padding
            )
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        
        val fillPath = android.graphics.Path(strokePath.asAndroidPath()).asComposePath().apply {
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Draw Area Gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF5252).copy(alpha = 0.4f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw Line
        drawPath(
            path = strokePath,
            color = Color(0xFFFF5252),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

fun formatDate(isoDate: String): String {
    return try {
        // Simple parser, in real app use generic util
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = input.parse(isoDate) ?: return isoDate
        output.format(date)
    } catch (e: Exception) {
        isoDate.take(10)
    }
}

fun formatRelativeTimeStr(isoString: String): String {
    try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = format.parse(isoString) ?: return ""
        val diff = System.currentTimeMillis() - date.time
        
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    } catch (e: Exception) {
        return "Unknown"
    }
}
