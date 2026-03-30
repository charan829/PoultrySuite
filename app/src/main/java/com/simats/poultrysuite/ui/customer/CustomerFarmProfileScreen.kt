package com.simats.poultrysuite.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.Review
import com.simats.poultrysuite.ui.market.MarketState
import com.simats.poultrysuite.ui.market.MarketViewModel
import com.simats.poultrysuite.ui.market.ReviewsState
import com.simats.poultrysuite.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomerFarmProfileScreen(
    navController: NavController,
    farmId: String,
    marketViewModel: MarketViewModel = hiltViewModel(),
    messagingViewModel: MessagingViewModel = hiltViewModel()
) {
    val marketState by marketViewModel.marketState.collectAsState()
    val reviewsState by marketViewModel.reviewsState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(farmId) {
        marketViewModel.loadListings()
        marketViewModel.loadFarmReviews(farmId)
    }

    val farmListings = (marketState as? MarketState.Success)
        ?.listings
        ?.filter { it.farmId == farmId }
        .orEmpty()

    val farm = farmListings.firstOrNull()?.farm

    val reviews = when (reviewsState) {
        is ReviewsState.Success -> (reviewsState as ReviewsState.Success).reviews
        else -> emptyList()
    }

    val averageRating = reviews.takeIf { it.isNotEmpty() }?.map { it.rating }?.average()
    val reviewCountLabel = if (reviews.size == 1) "1 review" else "${reviews.size} reviews"

    val ordersCount = reviews.size
    val productsCount = farmListings.size
    val responseRate = if (averageRating == null) 95 else (90 + (averageRating / 5.0 * 10.0)).toInt().coerceIn(90, 99)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Farm Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
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
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        when {
            farm == null && marketState is MarketState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            }

            farm == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Farm profile unavailable", color = Color(0xFF64748B))
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌾", fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = farm.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text(
                                            text = farm.location ?: "Location not specified",
                                            color = Color(0xFF64748B),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = averageRating?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "New",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = if (reviews.isEmpty()) "No reviews yet" else reviewCountLabel,
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (!farm.images.isNullOrEmpty()) {
                                Text(
                                    text = "Farm Photos",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(farm.images) { imageUrl ->
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Farm image",
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            Text(
                                text = "We are a family-owned poultry farm focused on healthy birds and reliable delivery service.",
                                color = Color(0xFF334155),
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatItem(value = "$ordersCount", label = "Orders")
                            StatItem(value = "$productsCount", label = "Products")
                            StatItem(value = "$responseRate%", label = "Response")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            messagingViewModel.startConversationAndOpen(
                                farmId = farmId,
                                onResult = { conversationId, farmName ->
                                    navController.navigate(Screen.CustomerChat.createRoute(conversationId, farmName))
                                },
                                onError = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1565C0))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Message Farm", fontWeight = FontWeight.SemiBold)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Customer Reviews",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            when (reviewsState) {
                                is ReviewsState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF1565C0),
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }

                                is ReviewsState.Error -> {
                                    Text(
                                        text = (reviewsState as ReviewsState.Error).message,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 13.sp
                                    )
                                }

                                is ReviewsState.Success -> {
                                    if (reviews.isEmpty()) {
                                        Text(
                                            text = "No customer reviews yet.",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp
                                        )
                                    } else {
                                        reviews.forEachIndexed { index, review ->
                                            FarmReviewRow(review = review)
                                            if (index != reviews.lastIndex) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }
                                        }
                                    }
                                }

                                ReviewsState.Idle -> Unit
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color(0xFF64748B), fontSize = 12.sp)
    }
}

@Composable
private fun FarmReviewRow(review: Review) {
    val displayDate = formatReviewDate(review.createdAt)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = review.customerName.take(1).uppercase(),
                color = Color(0xFF334155),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(review.customerName, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), fontSize = 14.sp)
                    Text(displayDate, color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            if (!review.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = review.comment,
                    color = Color(0xFF334155),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            if (!review.images.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(review.images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Review image",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}

private fun formatReviewDate(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = parser.parse(iso)
        if (date != null) formatter.format(date) else iso
    } catch (_: Exception) {
        iso
    }
}
