package com.viso.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viso.domain.model.Achievement
import com.viso.domain.model.Rarity
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary

@Composable
fun StreakBadge(
    streak: Int,
    maxStreak: Int,
    modifier: Modifier = Modifier
) {
    val fireCount = when {
        streak >= 24 -> 4
        streak >= 12 -> 3
        streak >= 6 -> 2
        streak >= 3 -> 1
        else -> 0
    }

    val fireIcon = when (fireCount) {
        4 -> "👑"
        3 -> "🔥🔥🔥"
        2 -> "🔥🔥"
        1 -> "🔥"
        else -> "⚪"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(getRarityColor(fireCount).copy(alpha = 0.15f))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fireIcon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.width(Spacing.sm))
        Column {
            Text(
                text = "$streak ${if (streak == 1) "mês" else "meses"}",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            if (maxStreak > streak) {
                Text(
                    text = "Recorde: $maxStreak",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = achievement.progressPercentage,
        label = "progress"
    )

    val backgroundColor = if (achievement.isUnlocked) {
        getRarityColor(achievement.rarity).copy(alpha = 0.1f)
    } else {
        Color.Gray.copy(alpha = 0.05f)
    }

    val contentColor = if (achievement.isUnlocked) {
        TextPrimary
    } else {
        TextSecondary
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .padding(Spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked)
                            getRarityColor(achievement.rarity).copy(alpha = 0.2f)
                        else
                            Color.Gray.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(Modifier.width(Spacing.md))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                    if (achievement.isUnlocked) {
                        Spacer(Modifier.width(Spacing.sm))
                        RarityBadge(rarity = achievement.rarity)
                    }
                }
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        if (!achievement.isUnlocked) {
            Spacer(Modifier.height(Spacing.sm))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = getRarityColor(achievement.rarity),
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = "${achievement.progress}/${achievement.target}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RarityBadge(rarity: Rarity) {
    val (text, color) = when (rarity) {
        Rarity.COMMON -> "Comum" to Color(0xFF9E9E9E)
        Rarity.RARE -> "Rara" to Color(0xFF2196F3)
        Rarity.EPIC -> "Épica" to Color(0xFF9C27B0)
        Rarity.LEGENDARY -> "Lendária" to Color(0xFFFF9800)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = Spacing.sm, vertical = 2.dp)
    )
}

private fun getRarityColor(rarity: Rarity): Color {
    return when (rarity) {
        Rarity.COMMON -> Color(0xFF9E9E9E)
        Rarity.RARE -> Color(0xFF2196F3)
        Rarity.EPIC -> Color(0xFF9C27B0)
        Rarity.LEGENDARY -> Color(0xFFFF9800)
    }
}

private fun getRarityColor(fireCount: Int): Color {
    return when (fireCount) {
        4 -> Color(0xFFFF9800) // Legendary
        3 -> Color(0xFF9C27B0) // Epic
        2 -> Color(0xFF2196F3) // Rare
        1 -> Color(0xFFFF5722) // Common (fire)
        else -> Color.Gray
    }
}
