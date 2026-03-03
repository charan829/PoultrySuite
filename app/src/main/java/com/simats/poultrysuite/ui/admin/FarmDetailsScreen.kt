package com.simats.poultrysuite.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.FarmDetails
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDetailsScreen(
    navController: NavController,
    farmId: String,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val detailsState by viewModel.farmDetailsState.collectAsState()

    LaunchedEffect(farmId) {
        viewModel.loadFarmDetails(farmId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (detailsState as? FarmDetailsState.Success)?.details?.name ?: "Farm Details"
                    Text(
                        title,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = detailsState) {
                is FarmDetailsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF1565C0)
                    )
                }
                is FarmDetailsState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FarmDetailsState.Success -> {
                    FarmDetailsContent(state.details)
                }
            }
        }
    }
}

@Composable
fun FarmDetailsContent(farm: FarmDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = farm.name.firstOrNull()?.toString() ?: "F",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = farm.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Active", // Using Mocked Status for now
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = farm.scale,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                InfoRow(Icons.Default.Person, farm.ownerName)
                InfoRow(Icons.Default.LocationOn, farm.location)
                InfoRow(Icons.Default.Phone, farm.phone)
                InfoRow(Icons.Default.Email, farm.email)
                
                // Format Date
                InfoRow(Icons.Default.DateRange, "Joined ${farm.joinedDate.take(10)}") 
            }
        }

        // 2. Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem(
                label = "Total Birds",
                value = NumberFormat.getIntegerInstance().format(farm.totalBirds),
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Monthly Rev.",
                value = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(farm.monthlyRevenue).replace("INR", "₹").substringBefore("."), // Truncate decimals for clean look like UI
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Products",
                value = farm.productsCount.toString(),
                color = Color(0xFFF57F17),
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Monthly Production Graph
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Monthly Production",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val data = farm.productionGraph
                    val maxVal = data.maxOrNull() ?: 1.0
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width / (data.size * 1.5f)
                        val spacing = size.width / data.size
                        
                        data.forEachIndexed { index, value ->
                            val barHeight = (value / maxVal) * size.height
                            drawRoundRect(
                                color = Color(0xFF1565C0),
                                topLeft = Offset(
                                    x = index * spacing + (spacing - barWidth) / 2,
                                    y = size.height - barHeight.toFloat()
                                ),
                                size = Size(barWidth, barHeight.toFloat()),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                    }
                }
                
                // Simple X-Axis Labels match mocks (Jan, Feb...)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun") // Mocked labels matching data size
                    months.take(farm.productionGraph.size).forEach { 
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray) 
                    }
                }
            }
        }

        // 4. Products
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Products",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (farm.productTypes.isEmpty()) {
                         Text("No products listed", color = Color.Gray)
                    } else {
                        farm.productTypes.forEach { type ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(type) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFFE3F2FD),
                                    labelColor = Color(0xFF1565C0)
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. Suspend Button
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFC62828)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Suspend Farm", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
