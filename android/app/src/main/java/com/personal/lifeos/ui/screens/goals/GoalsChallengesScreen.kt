package com.personal.lifeos.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
                        text = "15-Day Challenges & Goals",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDarkPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBackground)
            )
        },
        containerColor = SkyBackground
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
                    text = "ACTIVE 15-DAY SPRINTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            items(challenges, key = { it.id }) { ch ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), ambientColor = GlowSkyBlue),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SkyBlueSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = SkyBluePrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ch.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SkyBlueSurface
                            ) {
                                Text(
                                    text = "${ch.daysCompleted} / ${ch.durationDays} days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyBluePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = ch.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDarkSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val pct = if (ch.durationDays > 0) (ch.daysCompleted.toFloat() / ch.durationDays.toFloat()).coerceIn(0f, 1f) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SkyBlueSurface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(SkyBluePrimary, CyanAccent))
                                    )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "LONG-TERM GOALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                SkyGoalCard(title = "Reach Target Weight 75 kg", progress = "79.5 kg -> 75.0 kg", category = "HEALTH")
            }
            item {
                SkyGoalCard(title = "Complete Python Advanced Roadmap", progress = "Active Roadmap", category = "LEARNING")
            }
            item {
                SkyGoalCard(title = "100 Hours Monthly Learning Target", progress = "In Progress", category = "GROWTH")
            }
        }
    }
}

@Composable
fun SkyGoalCard(title: String, progress: String, category: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(category, style = MaterialTheme.typography.labelSmall, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
            }
            Text(progress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = SkyBluePrimary)
        }
    }
}
