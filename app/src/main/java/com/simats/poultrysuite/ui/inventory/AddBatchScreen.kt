package com.simats.poultrysuite.ui.inventory

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarToday
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBatchScreen(
    navController: NavController,
    viewModel: AddBatchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var batchName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("LAYER") }
    var quantity by remember { mutableStateOf("") }
    var dateAcquired by remember { mutableStateOf("") }
    var ageDays by remember { mutableStateOf("0") }
    var source by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val birdTypes = listOf("LAYER" to "Layers", "BROILER" to "Broilers", "DUCK" to "Duck", "TURKEY" to "Turkey")

    // Date picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            dateAcquired = "%02d/%02d/%04d".format(day, month + 1, year)
            // Calculate age in days from acquisition date
            val now = Calendar.getInstance()
            val acquired = Calendar.getInstance().apply { set(year, month, day) }
            ageDays = ((now.timeInMillis - acquired.timeInMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(0).toString()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = calendar.timeInMillis }

    LaunchedEffect(state) {
        when (state) {
            is AddBatchState.Success -> {
                Toast.makeText(context, "✅ Batch added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is AddBatchState.Error -> {
                Toast.makeText(context, (state as AddBatchState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Batch", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Batch Name
            BatchFormField(label = "Batch Name") {
                BatchOutlinedInput(
                    value = batchName,
                    onValueChange = { batchName = it },
                    placeholder = "e.g., Batch E - Layers"
                )
            }

            // Bird Type Selector
            BatchFormField(label = "Bird Type") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    birdTypes.forEach { (type, label) ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color(0xFF1565C0) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(
                                    if (isSelected) Color(0xFFE8F0FE) else Color.White,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1565C0) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Quantity
            BatchFormField(label = "Quantity") {
                BatchOutlinedInput(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    placeholder = "Number of birds",
                    keyboardType = KeyboardType.Number
                )
            }

            // Date Acquired
            BatchFormField(label = "Date Acquired") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateAcquired.ifBlank { "DD/MM/YYYY" },
                            color = if (dateAcquired.isBlank()) Color(0xFFCBD5E1) else Color(0xFF1E293B),
                            fontSize = 14.sp
                        )
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Source
            BatchFormField(label = "Source") {
                BatchOutlinedInput(
                    value = source,
                    onValueChange = { source = it },
                    placeholder = "Where did you get them?"
                )
            }

            // Notes
            BatchFormField(label = "Notes") {
                BatchOutlinedInput(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Additional notes...",
                    singleLine = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.saveBatch(
                        type = selectedType,
                        count = quantity,
                        ageDays = ageDays
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                enabled = quantity.isNotBlank() && state !is AddBatchState.Saving
            ) {
                if (state is AddBatchState.Saving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Batch", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchFormField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1565C0)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchOutlinedInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFFCBD5E1), fontSize = 14.sp) },
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 4,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedBorderColor = Color(0xFF1565C0)
        )
    )
}
