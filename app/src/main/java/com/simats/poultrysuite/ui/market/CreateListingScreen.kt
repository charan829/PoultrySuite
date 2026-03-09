package com.simats.poultrysuite.ui.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    navController: NavController,
    viewModel: MarketViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableStateOf("Broilers") }
    var quantity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val productTypes = listOf("Broilers", "Layers", "Eggs", "Chicks")
    val activeColor = Color(0xFF1565C0)
    val inactiveColor = Color(0xFFF1F5F9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Listing",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Product Type selector
            FormSection(title = "Product Type") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    productTypes.forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor,
                                selectedLabelColor = Color.White,
                                containerColor = inactiveColor,
                                labelColor = Color(0xFF64748B)
                            ),
                            border = null
                        )
                    }
                }
            }

            // Quantity
            FormSection(title = "Quantity") {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 50", color = Color(0xFFCBD5E1)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text(if (selectedType == "Eggs") "crates" else "birds", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )
            }

            // Price Per Unit
            FormSection(title = "Price Per Unit (₹)") {
                OutlinedTextField(
                    value = pricePerUnit,
                    onValueChange = { pricePerUnit = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 3500", color = Color(0xFFCBD5E1)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("₹", color = Color(0xFF64748B)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )
            }

            // Description (optional)
            FormSection(title = "Description (Optional)") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("Add details about your listing...", color = Color(0xFFCBD5E1)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors(),
                    maxLines = 4
                )
            }

            Spacer(Modifier.height(8.dp))

            // Create button
            Button(
                onClick = {
                    if (quantity.isNotBlank() && pricePerUnit.isNotBlank()) {
                        isSaving = true
                        viewModel.createListing(
                            type = selectedType,
                            quantity = quantity,
                            price = pricePerUnit,
                            onSuccess = { navController.navigateUp() },
                            onError = { isSaving = false }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                enabled = !isSaving && quantity.isNotBlank() && pricePerUnit.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Listing", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), fontSize = 14.sp)
        content()
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF1565C0),
    unfocusedBorderColor = Color(0xFFE2E8F0),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)
