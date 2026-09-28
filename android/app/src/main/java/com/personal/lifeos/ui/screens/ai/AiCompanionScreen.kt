package com.personal.lifeos.ui.screens.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.data.remote.DirectAiClient
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

data class SkyChatMessage(val sender: String, val text: String, val isAi: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCompanionScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    var messages by remember {
        mutableStateOf(
            listOf(
                SkyChatMessage(
                    "Life AI",
                    "Namaste! Main aapka personal AI companion hoon. Aap mujhse natural bhasha (Hindi ya English) me sawaal pooch sakte hain ya daily actions bol sakte hain:\n• 'What should I do now?'\n• 'Maine 1 ghanta Python padha'\n• '2 roti aur dal khaya'\n• 'Healthy evening routine plan karo'",
                    true
                )
            )
        )
    }

    val quickChips = listOf(
        "What should I do now?",
        "Maine 1 ghanta study kiya",
        "2 roti aur paneer khaya",
        "Plan my evening routine",
        "How to improve my sleep?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SkyBlueSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = SkyBluePrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Life AI Companion", color = TextDarkPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text("Connected to Hugging Face Cloud", color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showConfigDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SkyBluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBackground)
            )
        },
        containerColor = SkyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (msg.isAi) 4.dp else 20.dp,
                                bottomEnd = if (msg.isAi) 20.dp else 4.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isAi) PureWhite else SkyBluePrimary
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = if (msg.isAi) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite))
                            ) else null,
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = msg.sender,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (msg.isAi) SkyBluePrimary else PureWhite.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.isAi) TextDarkPrimary else PureWhite,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // Animated Bouncing Typing Indicator when AI is thinking
                if (isThinking) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            modifier = Modifier.width(100.dp).padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BouncingDot(delay = 0)
                                BouncingDot(delay = 200)
                                BouncingDot(delay = 400)
                            }
                        }
                    }
                }
            }

            // Quick Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickChips) { chipText ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, SkyBlueSurface))),
                        modifier = Modifier.shadow(1.dp, RoundedCornerShape(16.dp))
                    ) {
                        TextButton(
                            onClick = {
                                inputText = chipText
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(chipText, color = SkyBluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Input bar
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
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
                        placeholder = { Text("Ask Life AI or log activity...", color = TextMuted, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = CardBorderLight
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isThinking) {
                                val userQuery = inputText
                                inputText = ""
                                messages = messages + SkyChatMessage("You", userQuery, false)
                                isThinking = true

                                scope.launch {
                                    val aiResponse = DirectAiClient.queryHuggingFaceDirect(context, userQuery)
                                    messages = messages + SkyChatMessage("Life AI", aiResponse, true)
                                    isThinking = false
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SkyBluePrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = PureWhite, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        if (showConfigDialog) {
            var hfKey by remember { mutableStateOf(DirectAiClient.getHfKey(context)) }
            AlertDialog(
                onDismissRequest = { showConfigDialog = false },
                title = { Text("AI Engine Configuration", color = TextDarkPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Aapki Hugging Face API key configured hai. Ise yahan change ya update kar sakte hain:", fontSize = 13.sp, color = TextDarkSecondary)
                        OutlinedTextField(
                            value = hfKey,
                            onValueChange = { hfKey = it },
                            label = { Text("Hugging Face API Key") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            DirectAiClient.saveHfKey(context, hfKey)
                            showConfigDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                    ) {
                        Text("Save Key")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfigDialog = false }) { Text("Cancel") }
                },
                containerColor = PureWhite
            )
        }
    }
}

@Composable
fun BouncingDot(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "BouncingDot")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotOffset"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(SkyBluePrimary)
    )
}
