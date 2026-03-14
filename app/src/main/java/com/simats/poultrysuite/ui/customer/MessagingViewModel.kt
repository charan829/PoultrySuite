package com.simats.poultrysuite.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.poultrysuite.data.model.ChatMessage
import com.simats.poultrysuite.data.model.ConversationSummary
import com.simats.poultrysuite.data.model.SendMessageRequest
import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConversationsState {
    object Loading : ConversationsState()
    data class Success(val conversations: List<ConversationSummary>) : ConversationsState()
    data class Error(val message: String) : ConversationsState()
}

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val messages: List<ChatMessage>) : ChatState()
    data class Error(val message: String) : ChatState()
}

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val api: PoultryApi
) : ViewModel() {

    private val _conversationsState = MutableStateFlow<ConversationsState>(ConversationsState.Loading)
    val conversationsState = _conversationsState.asStateFlow()

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState = _chatState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    fun loadConversations(silent: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!silent || _conversationsState.value !is ConversationsState.Success) {
                    _conversationsState.value = ConversationsState.Loading
                }
                val list = api.getConversations()
                _conversationsState.value = ConversationsState.Success(list)
            } catch (e: Exception) {
                if (_conversationsState.value !is ConversationsState.Success) {
                    _conversationsState.value = ConversationsState.Error(e.message ?: "Failed to load messages")
                }
            }
        }
    }

    fun loadMessages(conversationId: String, silent: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!silent || _chatState.value !is ChatState.Success) {
                    _chatState.value = ChatState.Loading
                }
                val messages = api.getMessages(conversationId)
                _chatState.value = ChatState.Success(messages)
            } catch (e: Exception) {
                if (_chatState.value !is ChatState.Success) {
                    _chatState.value = ChatState.Error(e.message ?: "Failed to load messages")
                }
            }
        }
    }

    fun sendMessage(
        conversationId: String,
        content: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isSending.value = true
                val sent = api.sendMessage(conversationId, SendMessageRequest(content))
                val current = (_chatState.value as? ChatState.Success)?.messages ?: emptyList()
                _chatState.value = ChatState.Success((current + sent).distinctBy { it.id })
                updateConversationPreview(conversationId, sent)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to send message")
            } finally {
                _isSending.value = false
            }
        }
    }

    fun startConversationAndOpen(
        farmId: String,
        onResult: (conversationId: String, farmName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = api.startConversation(farmId)
                loadConversations(silent = true)
                onResult(result.id, result.farmName)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to open chat")
            }
        }
    }

    private fun updateConversationPreview(conversationId: String, message: ChatMessage) {
        val current = (_conversationsState.value as? ConversationsState.Success)?.conversations ?: return
        val updated = current.map { conversation ->
            if (conversation.id == conversationId) {
                conversation.copy(
                    lastMessage = message.content,
                    lastMessageTime = message.createdAt
                )
            } else {
                conversation
            }
        }.sortedByDescending { it.lastMessageTime ?: "" }

        _conversationsState.value = ConversationsState.Success(updated)
    }
}
