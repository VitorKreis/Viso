package com.viso.ui.streaks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.ui.components.AchievementCard
import com.viso.ui.components.StreakBadge
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreaksScreen(
    onBack: () -> Unit,
    viewModel: StreaksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgApp,
        topBar = {
            TopAppBar(
                title = { Text("Conquistas", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAchievements() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Atualizar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgApp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Streak Card
            item {
                StreakCard(
                    currentStreak = state.streakInfo?.currentStreak ?: 0,
                    maxStreak = state.streakInfo?.maxStreak ?: 0,
                    thisMonthProgress = state.streakInfo?.thisMonthProgress ?: 0f,
                    daysRemaining = state.streakInfo?.daysRemaining ?: 0
                )
            }

            // Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    StatCard(
                        value = state.unlockedCount.toString(),
                        label = "Conquistas",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = "${state.totalCount - state.unlockedCount}",
                        label = "Restantes",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section title
            item {
                Text(
                    text = "Suas Conquistas",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }

            // Achievements list
            items(state.achievements) { achievement ->
                AchievementCard(achievement = achievement)
            }

            item { Spacer(Modifier.height(Spacing.xxxl)) }
        }
    }
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    maxStreak: Int,
    thisMonthProgress: Float,
    daysRemaining: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(BgCard)
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sequência Atual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(Spacing.xs))
                StreakBadge(streak = currentStreak, maxStreak = maxStreak)
            }

            // Streak indicator circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        // Month progress
        Text(
            text = "Progresso deste mês",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(Spacing.sm))

        androidx.compose.material3.LinearProgressIndicator(
            progress = { thisMonthProgress },
            modifier = Modifier.fillMaxWidth(),
            color = AccentBlue,
            trackColor = AccentBlue.copy(alpha = 0.2f)
        )

        Spacer(Modifier.height(Spacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(thisMonthProgress * 100).toInt()}% completo",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "$daysRemaining dias restantes",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        if (currentStreak > 0) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = when {
                    currentStreak >= 12 -> "Incrível! Você é um mestre da disciplina financeira! 🏆"
                    currentStreak >= 6 -> "Excelente! Sua consistência está pagando! 💪"
                    currentStreak >= 3 -> "Bom trabalho! Continue assim! 🔥"
                    else -> "Você está no caminho certo! 👍"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(BgCard)
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = AccentBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
