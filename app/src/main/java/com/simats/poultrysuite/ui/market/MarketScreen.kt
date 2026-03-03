package com.simats.poultrysuite.ui.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.ProductRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    navController: NavController,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val state by viewModel.marketState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Marketplace") })
        },
        floatingActionButton = {
            if (userRole == "FARMER") {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Listing")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val s = state) {
                is MarketState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MarketState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${s.message}")
                    }
                }
                is MarketState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.listings) { item ->
                            ListingCard(item = item, isCustomer = userRole == "CUSTOMER") {
                                viewModel.placeOrder(item.id)
                            }
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddListingDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { type, qty, price -> 
                    viewModel.createListing(type, qty, price)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ListingCard(item: ProductRequest, isCustomer: Boolean, onBuy: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.type, style = MaterialTheme.typography.titleMedium)
            Text(text = "Farm: ${item.farm?.name ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Qty: ${item.quantity}")
                Text("Price: $${item.pricePerUnit}/unit")
            }
            if (isCustomer) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBuy, modifier = Modifier.align(Alignment.End)) {
                    Text("Buy Now")
                }
            }
        }
    }
}

@Composable
fun AddListingDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var type by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Listing") },
        text = {
            Column {
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Product Type") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price Per Unit") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(type, quantity, price) }) {
                Text("List Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
