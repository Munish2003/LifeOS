package com.personal.lifeos.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

data class ChatMessage(val sender: String, val text: String, val isAi: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCompanionScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "Life AI",
                    "Hello! I am your Life OS Companion. You can type naturally like: 'I studied Python for 1 hour' or 'Tomorrow I have an outing for 4 hours', or ask 'What should I do now?'.",
                    true
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = SecondaryCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Life AI Assistant", color = Slate100, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        containerColor = Slate950
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick "What should I do now?" action chip
                item {
                    Button(
                        onClick = {
                            val rec = state.availability?.recommendedReason ?: "Focus on Python. You have a 45m slot open."
                            messages = messages + ChatMessage("You", "What should I do now?", false)
                            messages = messages + ChatMessage("Life AI", rec, true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo.copy(alpha = 0.25f))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "What Next", tint = PrimaryLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask: 'What should I do now?'", color = PrimaryLight, fontWeight = FontWeight.Bold)
                    }
                }

                items(messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isAi) CardBackground else PrimaryIndigo
                            ),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = msg.sender,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (msg.isAi) SecondaryCyan else Slate100,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate100,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // Chat Input Box
            Surface(
                color = CardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Log meal, task, outing or question...", color = Slate400, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText
                                inputText = ""
                                messages = messages + ChatMessage("You", text, false)
                                scope.launch {
                                    val reply = homeViewModel.repository.processCommand(text)
                                    messages = messages + ChatMessage("Life AI", reply, true)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryLight)
                    }
                }
            }
        }
    }
}
