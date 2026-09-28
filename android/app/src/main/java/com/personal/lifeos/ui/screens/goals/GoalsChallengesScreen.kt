package com.personal.lifeos.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.data.local.entity.ChallengeEntity
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsChallengesScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    var challenges by remember { mutableStateOf<List<ChallengeEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        homeViewModel.repository.getActiveChallenges().collect {
            challenges = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Goals & Challenges",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        containerColor = Slate950
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "ACTIVE 15-DAY CHALLENGES",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryLight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(challenges, key = { it.id }) { ch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = AccentAmber)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = ch.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                            }
                            Text(
                                text = "${ch.daysCompleted} / ${ch.durationDays} days",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ch.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val pct = (ch.daysCompleted.toFloat() / ch.durationDays.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Slate800)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(PrimaryIndigo, SecondaryCyan))
                                    )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "LONG-TERM GOALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                GoalCard(title = "Reach Target Weight 75 kg", progress = "79.5 kg / 75.0 kg", category = "HEALTH")
            }
            item {
                GoalCard(title = "Complete Python Advanced Roadmap", progress = "76% Complete", category = "LEARNING")
            }
            item {
                GoalCard(title = "100 Hours Monthly Learning Target", progress = "58h / 100h", category = "GROWTH")
            }
        }
    }
}

@Composable
fun GoalCard(title: String, progress: String, category: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Slate100)
                Spacer(modifier = Modifier.height(4.dp))
                Text(category, style = MaterialTheme.typography.labelSmall, color = Slate400)
            }
            Text(progress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SecondaryCyan)
        }
    }
}
