package com.simats.poultrysuite.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.poultrysuite.data.model.ChatMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatScreen(
    navController: NavController,
    conversationId: String,
    farmName: String,
    viewModel: MessagingViewModel = hiltViewModel()
) {
    val chatState by viewModel.chatState.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var lastRenderedMessageId by remember(conversationId) { mutableStateOf<String?>(null) }

    fun sendCurrentMessage() {
        val content = messageText.trim()
        if (content.isEmpty() || isSending) return
        messageText = ""
        viewModel.sendMessage(
            conversationId = conversationId,
            content = content,
            onSuccess = {},
            onError = { message ->
                messageText = content
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    LaunchedEffect(conversationId) {
        while (true) {
            delay(3500)
            viewModel.loadMessages(conversationId, silent = true)
        }
    }

    LaunchedEffect(chatState) {
        val latestMessageId = (chatState as? ChatState.Success)?.messages?.lastOrNull()?.id ?: return@LaunchedEffect
        if (latestMessageId != lastRenderedMessageId) {
            lastRenderedMessageId = latestMessageId
            val lastIndex = (chatState as? ChatState.Success)?.messages?.lastIndex ?: return@LaunchedEffect
            if (lastIndex >= 0) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = farmName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .imePadding()
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(26.dp))
                            .padding(start = 6.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("Type a message...", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            maxLines = 4,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(onSend = { sendCurrentMessage() })
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val canSend = messageText.isNotBlank()
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(21.dp),
                        color = if (canSend) Color(0xFFE8F0FE) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        IconButton(
                            onClick = { sendCurrentMessage() },
                            enabled = canSend && !isSending
                        )
                        {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF3F5F9)
    ) { padding ->
        when (val s = chatState) {
            is ChatState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            }
            is ChatState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.message, color = Color(0xFF64748B))
                }
            }
            is ChatState.Success -> {
                if (s.messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👋", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Say hello to $farmName!",
                                fontSize = 15.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(s.messages, key = { _, msg -> msg.id }) { index, msg ->
                            val showDateDivider = if (index == 0) true else !isSameDay(s.messages[index - 1].createdAt, msg.createdAt)
                            if (showDateDivider) {
                                ChatDateDivider(date = formatChatDate(msg.createdAt))
                            }
                            ChatBubble(message = msg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isMine) Color(0xFF216DCC) else Color.White
    val textColor = if (message.isMine) Color.White else Color(0xFF1E293B)
    val shape = if (message.isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .clip(shape)
                .background(bubbleColor, shape)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        if (message.isMine) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = formatChatTime(message.createdAt),
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (message.isRead) "Seen" else "Sent",
                    fontSize = 10.sp,
                    color = if (message.isRead) Color(0xFF2563EB) else Color(0xFF94A3B8)
                )
            }
        } else {
            Text(
                text = formatChatTime(message.createdAt),
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun ChatDateDivider(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .background(Color(0xFFECEFF4), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun isSameDay(isoA: String, isoB: String): Boolean {
    val dateA = try { Instant.parse(isoA).atZone(ZoneId.systemDefault()).toLocalDate() } catch (_: Exception) { null }
    val dateB = try { Instant.parse(isoB).atZone(ZoneId.systemDefault()).toLocalDate() } catch (_: Exception) { null }
    return dateA != null && dateA == dateB
}

private fun formatChatDate(iso: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.parse(iso))
    } catch (e: Exception) { "" }
}

private fun formatChatTime(iso: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.parse(iso))
    } catch (e: Exception) { "" }
}
