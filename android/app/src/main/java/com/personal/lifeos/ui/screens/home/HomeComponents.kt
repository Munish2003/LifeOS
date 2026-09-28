package com.personal.lifeos.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.lifeos.ui.theme.*

@Composable
fun RoutineTimelineDiagram() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROUTINE & AVAILABILITY TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Engine Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Blocks
            TimelineBlock(time = "09:00 - 13:00", title = "Office Deep Work", type = "Fixed Commitment (4h)", isCurrent = false)
            TimelineBlock(time = "14:00 - 16:30", title = "\uD83D\uDD25 Available Focus Window", type = "High Productivity Period (2h 30m)", isCurrent = true)
            TimelineBlock(time = "18:30 - 19:30", title = "Evening Walk & Steps", type = "Health & Exercise Block (1h)", isCurrent = false, isLast = true)
        }
    }
}

@Composable
fun TimelineBlock(time: String, title: String, type: String, isCurrent: Boolean, isLast: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
    ) {
        // Time
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCurrent) SkyBlueVibrant else TextMuted,
            modifier = Modifier.width(80.dp),
            fontSize = 11.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        // Line and dot
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(12.dp)) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isCurrent) SkyBlueVibrant else TextMuted.copy(alpha = 0.5f))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier.width(1.dp).fillMaxHeight().background(if (isCurrent) SkyBlueVibrant.copy(alpha = 0.5f) else TextMuted.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrent) TextLight else TextDarkPrimary,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = type,
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrent) SkyBluePrimary else TextMuted
            )
        }
    }
}

@Composable
fun QuickPriorityTasks() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "AI DAILY TASK INTELLIGENCE",
                style = MaterialTheme.typography.labelSmall,
                color = SkyBluePrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            TaskListItem("Python Deep Work (FastAPI)", "Productivity", "P1", true)
            Spacer(modifier = Modifier.height(12.dp))
            TaskListItem("Evening Walk & Step Target", "Health", "P2", false)
            Spacer(modifier = Modifier.height(12.dp))
            TaskListItem("Review System Specs", "Learning", "P3", false)
        }
    }
}

@Composable
fun TaskListItem(title: String, category: String, priority: String, isDone: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isDone) EmeraldSuccess else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(if (isDone) EmeraldSuccess else OffWhite))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDone) TextMuted else TextDarkPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (priority == "P1") RoseError.copy(alpha=0.15f) else SkyBlueSurface
        ) {
            Text(
                text = priority,
                style = MaterialTheme.typography.labelSmall,
                color = if (priority == "P1") RoseError else SkyBluePrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun ConsistencyHabitMatrix() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONSISTENCY & HABIT MATRIX",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Daily Activity Rings",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SkyBlueSurface
                ) {
                    Text(
                        text = "Weekly",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBluePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THIS WEEK'S CONSISTENCY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "⚡ 86% Adherence",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlueVibrant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7 Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEachIndexed { index, day ->
                    val isToday = index == 3
                    val isDone = index < 3
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) SkyBluePrimary else TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isToday) SkyBlueSurface else if (isDone) EmeraldSuccess.copy(alpha=0.15f) else Color.Transparent)
                                .border(
                                    width = if (isToday) 2.dp else 1.dp,
                                    color = if (isToday) SkyBlueVibrant else if (isDone) EmeraldSuccess else TextMuted.copy(alpha=0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Text("✓", color = EmeraldSuccess, fontSize = 12.sp)
                            } else if (isToday) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SkyBlueVibrant))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("✓", "Done", EmeraldSuccess)
                LegendItem("◐", "Partial", AmberWarning)
                LegendItem("○", "Rest", TextMuted)
                LegendItem("●", "Today", SkyBlueVibrant)
            }
        }
    }
}

@Composable
fun LegendItem(symbol: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = symbol, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextMuted, fontSize = 10.sp)
    }
}
