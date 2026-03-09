package com.simats.poultrysuite.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.BatchDetail
import com.simats.poultrysuite.data.model.FeedLog
import com.simats.poultrysuite.data.model.MortalityRecord
import com.simats.poultrysuite.data.model.VaccinationRecord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    batchId: String,
    navController: NavController,
    viewModel: BatchDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(batchId) { viewModel.loadBatch(batchId) }
    val state by viewModel.state.collectAsState()

    var showActionDialog by remember { mutableStateOf(false) }
    var showMortalityDialog by remember { mutableStateOf(false) }
    var showVaccinationDialog by remember { mutableStateOf(false) }
    var showFeedDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batch Details", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        floatingActionButton = {
            if (state is BatchDetailState.Success) {
                FloatingActionButton(
                    onClick = { showActionDialog = true },
                    containerColor = Color(0xFF1565C0)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Log", tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        when (val s = state) {
            is BatchDetailState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFF1565C0)) }

            is BatchDetailState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = Color(0xFF94A3B8))
                    TextButton(onClick = { viewModel.loadBatch(batchId) }) {
                        Text("Retry", color = Color(0xFF1565C0))
                    }
                }
            }

            is BatchDetailState.Success -> {
                val batch = s.batch
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item { BatchHeaderCard(batch) }

                    // Mortality Records Section
                    item {
                        SectionHeader(
                            icon = Icons.Default.Shield,
                            title = "Mortality Records",
                            iconTint = Color(0xFF64748B)
                        )
                    }
                    if (batch.mortalityRecords.isEmpty()) {
                        item {
                            EmptySection("No mortality records")
                        }
                    } else {
                        items(batch.mortalityRecords) { record ->
                            MortalityRecordItem(record)
                        }
                    }

                    // Vaccination Schedule Section
                    item {
                        SectionHeader(
                            icon = Icons.Default.Vaccines,
                            title = "Vaccination Schedule",
                            iconTint = Color(0xFF64748B)
                        )
                    }
                    if (batch.vaccinationRecords.isEmpty()) {
                        item {
                            EmptySection("No vaccination records")
                        }
                    } else {
                        items(batch.vaccinationRecords) { record ->
                            VaccinationRecordItem(record)
                        }
                    }

                    // Feed Consumption Section
                    item {
                        SectionHeader(
                            icon = Icons.Default.SetMeal,
                            title = "Feed Consumption",
                            iconTint = Color(0xFF64748B)
                        )
                    }
                    item {
                        FeedSummaryCard(
                            totalFeedKg = batch.totalFeedKg,
                            avgDailyKg = batch.avgDailyFeedKg
                        )
                    }
                    if (batch.feedLogs.isNotEmpty()) {
                        items(batch.feedLogs) { log ->
                            FeedLogItem(log)
                        }
                    } else {
                        item { EmptySection("No feed logs recorded") }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Add New Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showActionDialog = false; showMortalityDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Log Mortality") }
                    Button(onClick = { showActionDialog = false; showVaccinationDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Log Vaccination") }
                    Button(onClick = { showActionDialog = false; showFeedDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Log Feed Consumption") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showActionDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showMortalityDialog) {
        var count by remember { mutableStateOf("") }
        var cause by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showMortalityDialog = false },
            title = { Text("Log Mortality") },
            text = {
                Column {
                    OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("Number of Birds") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = cause, onValueChange = { cause = it }, label = { Text("Cause (e.g., Illness, Injury)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (count.isNotBlank() && cause.isNotBlank()) {
                        viewModel.logMortality(batchId, count, cause, 
                            onSuccess = { showMortalityDialog = false; android.widget.Toast.makeText(context, "Saved", android.widget.Toast.LENGTH_SHORT).show() },
                            onError = { err -> android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show() }
                        )
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showMortalityDialog = false }) { Text("Cancel") } }
        )
    }

    if (showVaccinationDialog) {
        var name by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var status by remember { mutableStateOf("Completed") }
        AlertDialog(
            onDismissRequest = { showVaccinationDialog = false },
            title = { Text("Log Vaccination") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vaccine Name/Detail") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && date.isNotBlank()) {
                        viewModel.logVaccination(batchId, name, date, status,
                            onSuccess = { showVaccinationDialog = false; android.widget.Toast.makeText(context, "Saved", android.widget.Toast.LENGTH_SHORT).show() },
                            onError = { err -> android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show() }
                        )
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showVaccinationDialog = false }) { Text("Cancel") } }
        )
    }

    if (showFeedDialog) {
        var amount by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedDialog = false },
            title = { Text("Log Feed Consumption") },
            text = {
                Column {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (Kg)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (amount.isNotBlank()) {
                        viewModel.logFeed(batchId, amount, notes,
                            onSuccess = { showFeedDialog = false; android.widget.Toast.makeText(context, "Saved", android.widget.Toast.LENGTH_SHORT).show() },
                            onError = { err -> android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show() }
                        )
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showFeedDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BatchHeaderCard(batch: BatchDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE8F0FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Egg,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Batch - ${batch.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Started: ${formatDate(batch.startedAt)}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(value = "%,d".format(batch.count), label = "Current", color = Color(0xFF1E293B))
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFF1F5F9))
                StatColumn(value = "${batch.weeksOld}", label = "Weeks Old", color = Color(0xFF1E293B))
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFF1F5F9))
                StatColumn(value = "${batch.mortalityRate}%", label = "Mortality", color = Color(0xFF22C55E))
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun MortalityRecordItem(record: MortalityRecord) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "${record.count} ${if (record.count == 1) "bird" else "birds"}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )
            Text(
                record.cause,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
        Text(
            formatDate(record.date),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1565C0)
        )
    }
}

@Composable
private fun VaccinationRecordItem(record: VaccinationRecord) {
    val statusColor = when (record.status) {
        "Completed" -> Color(0xFF22C55E)
        "Missed" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                record.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )
            Text(
                formatDate(record.scheduledDate),
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
        Text(
            record.status,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = statusColor
        )
    }
}

@Composable
private fun FeedSummaryCard(totalFeedKg: Double, avgDailyKg: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${totalFeedKg} kg", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1565C0))
                Text("Total Recorded", fontSize = 11.sp, color = Color(0xFF64748B))
            }
            VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFBBD6FB))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${avgDailyKg} kg", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1565C0))
                Text("Est. Daily Need", fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun FeedLogItem(log: FeedLog) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "${log.amountKg} kg",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )
            if (!log.notes.isNullOrBlank()) {
                Text(log.notes, fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
        }
        Text(
            formatDate(log.date),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1565C0)
        )
    }
}

@Composable
private fun EmptySection(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color(0xFFCBD5E1), fontSize = 13.sp)
    }
}

private fun formatDate(isoDate: String): String {
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFmt.timeZone = TimeZone.getTimeZone("UTC")
        val outputFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        outputFmt.format(inputFmt.parse(isoDate) ?: return isoDate)
    } catch (_: Exception) { isoDate }
}
