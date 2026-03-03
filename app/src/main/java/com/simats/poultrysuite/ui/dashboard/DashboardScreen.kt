package com.simats.poultrysuite.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.Batch
import com.simats.poultrysuite.data.model.Inventory
import com.simats.poultrysuite.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.farmState.collectAsState()
    var showAddBatchDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = { showAddBatchDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Batch")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(onClick = { navController.navigate(Screen.Marketplace.route) }) {
                    Text("Market")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val s = state) {
                is DashboardState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is DashboardState.Success -> {
                    val farm = s.farm
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = farm.name, style = MaterialTheme.typography.headlineSmall)
                                    Text(text = "Location: ${farm.location ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        item {
                            Text("Active Batches", style = MaterialTheme.typography.titleMedium)
                        }

                        items(farm.batches ?: emptyList()) { batch ->
                            BatchCard(batch)
                        }

                        item {
                            Text("Inventory", style = MaterialTheme.typography.titleMedium)
                        }

                        items(farm.inventory ?: emptyList()) { item ->
                            InventoryCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatchCard(batch: Batch) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${batch.type} (x${batch.count})", style = MaterialTheme.typography.titleMedium)
            Text(text = "Age: ${batch.ageDays} days", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun InventoryCard(item: Inventory) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Feed: ${item.feedKg} kg", style = MaterialTheme.typography.titleMedium)
            item.medicine?.let {
                Text(text = "Medicine: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AddBatchDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var type by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Batch") },
        text = {
            Column {
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Bird Type (e.g. Broiler)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it },
                    label = { Text("Count") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age (Days)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(type, count, age) }) {
                Text("Add Batch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
