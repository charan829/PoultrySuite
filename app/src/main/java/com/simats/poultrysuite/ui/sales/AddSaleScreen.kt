package com.simats.poultrysuite.ui.sales

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(
    navController: NavController,
    viewModel: AddSaleViewModel = hiltViewModel()
) {
    val saleState by viewModel.saleState.collectAsState()
    val context = LocalContext.current

    var productType by remember { mutableStateOf("BROILER") }
    var quantity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val productTypes = listOf("BROILER", "LAYER", "DUCK", "TURKEY", "EGGS")
    val totalPrice = (quantity.toIntOrNull() ?: 0) * (pricePerUnit.toDoubleOrNull() ?: 0.0)

    LaunchedEffect(saleState) {
        when (saleState) {
            is SaleState.Success -> {
                Toast.makeText(context, "✅ Sale recorded successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is SaleState.Error -> {
                Toast.makeText(context, (saleState as SaleState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Sale", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product Type Selector
            item {
                SaleFormCard(title = "Product Type") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = !dropdownExpanded },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF8F9FA)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    productType,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(Color.White)
                        ) {
                            productTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = type,
                                            color = if (type == productType) Color(0xFF1565C0) else Color(0xFF1E293B),
                                            fontWeight = if (type == productType) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        productType = type
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Quantity & Price
            item {
                SaleFormCard(title = "Sale Details") {
                    SaleTextField(
                        label = "Quantity (units)",
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        keyboardType = KeyboardType.Number,
                        placeholder = "e.g. 100"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SaleTextField(
                        label = "Price Per Unit (₹)",
                        value = pricePerUnit,
                        onValueChange = { pricePerUnit = it.filter { c -> c.isDigit() || c == '.' } },
                        keyboardType = KeyboardType.Decimal,
                        placeholder = "e.g. 250.00"
                    )

                    // Total Price Summary
                    AnimatedVisibility(visible = totalPrice > 0) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount", color = Color(0xFF64748B), fontSize = 13.sp)
                                Text(
                                    text = "₹${String.format("%,.2f", totalPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }
                    }
                }
            }

            // Buyer Info
            item {
                SaleFormCard(title = "Buyer Info (Optional)") {
                    SaleTextField(
                        label = "Buyer Name",
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        placeholder = "e.g. Ravi Kumar"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SaleTextField(
                        label = "Notes",
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "e.g. Direct sale at farm gate",
                        singleLine = false
                    )
                }
            }

            // Submit Button
            item {
                val isSubmittable = quantity.isNotBlank() && pricePerUnit.isNotBlank()
                Button(
                    onClick = {
                        viewModel.submitSale(
                            productType = productType,
                            quantity = quantity.toIntOrNull() ?: 0,
                            pricePerUnit = pricePerUnit.toDoubleOrNull() ?: 0.0,
                            buyerName = buyerName,
                            notes = notes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    enabled = isSubmittable && saleState !is SaleState.Saving
                ) {
                    if (saleState is SaleState.Saving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Record Sale",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleFormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color(0xFFCBD5E1)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 3,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFFF8F9FA),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color(0xFF1565C0)
            )
        )
    }
}
