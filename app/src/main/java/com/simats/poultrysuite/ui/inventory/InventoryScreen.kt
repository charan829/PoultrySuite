package com.simats.poultrysuite.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simats.poultrysuite.data.model.InventoryBatch
import com.simats.poultrysuite.ui.dashboard.FarmerBottomNavigation
import com.simats.poultrysuite.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddFeedDialog by remember { mutableStateOf(false) }
    var addFeedInput by remember { mutableStateOf("") }
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var addMedicineInput by remember { mutableStateOf("") }
    val filters = listOf("All", "Active", "Sold")

    // Refresh when screen resumes
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Screen.FarmerInventory.route) {
            viewModel.loadInventory()
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
                    "Inventory",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Screen.FarmerAddBatch.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Batch", tint = Color(0xFF1E293B))
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
                is InventoryState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1565C0))
                    }
                }
                is InventoryState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to load inventory", color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.loadInventory() }) {
                                Text("Retry", color = Color(0xFF1565C0))
                            }
                        }
                    }
                }
                is InventoryState.Success -> {
                    val feedRemaining = s.data.feedKg
                    val medicineCount = s.data.medicineCount
                    val allBatches = s.data.batches
                    val filtered = when (selectedFilter) {
                        "Active" -> allBatches.filter { it.status == "Active" }
                        "Sold" -> allBatches.filter { it.status == "Sold" }
                        else -> allBatches
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Feed Remaining", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(text = "${String.format("%.1f", feedRemaining)} kg", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = {
                                        addFeedInput = ""
                                        showAddFeedDialog = true
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(text = "Add Feed", color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Medicine Count", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(text = "${medicineCount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = {
                                        addMedicineInput = ""
                                        showAddMedicineDialog = true
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(text = "Add Medicine", color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No ${selectedFilter.lowercase()} batches found",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filtered) { batch ->
                                BatchListItem(
                                    batch = batch,
                                    onClick = {
                                        navController.navigate(
                                            Screen.FarmerBatchDetail.createRoute(batch.id)
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

        if (showAddFeedDialog) {
            AlertDialog(
                onDismissRequest = { showAddFeedDialog = false },
                title = { Text("Add Feed Stock") },
                text = {
                    OutlinedTextField(
                        value = addFeedInput,
                        onValueChange = { addFeedInput = it },
                        singleLine = true,
                        label = { Text("Amount (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = addFeedInput.toDoubleOrNull()
                            if (amount == null || amount <= 0.0) {
                                android.widget.Toast.makeText(context, "Enter a valid feed amount", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.addFeed(
                                amountKg = addFeedInput,
                                onSuccess = {
                                    showAddFeedDialog = false
                                    android.widget.Toast.makeText(context, "Feed added successfully", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = { message ->
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFeedDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAddMedicineDialog) {
            AlertDialog(
                onDismissRequest = { showAddMedicineDialog = false },
                title = { Text("Add Medicine Stock") },
                text = {
                    OutlinedTextField(
                        value = addMedicineInput,
                        onValueChange = { addMedicineInput = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        label = { Text("Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val count = addMedicineInput.toIntOrNull()
                            if (count == null || count <= 0) {
                                android.widget.Toast.makeText(context, "Enter a valid medicine count", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.addMedicine(
                                count = addMedicineInput,
                                onSuccess = {
                                    showAddMedicineDialog = false
                                    android.widget.Toast.makeText(context, "Medicine count updated", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = { message ->
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddMedicineDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun BatchListItem(batch: InventoryBatch, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = batch.name,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${batch.weeksOld} weeks • Mortality: ${batch.mortalityRate}%",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (batch.count > 0) "${"%,d".format(batch.count)} birds" else "—",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = batch.status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (batch.status == "Active") Color(0xFF22C55E) else Color(0xFF94A3B8)
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
