package com.viso.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.viso.domain.model.Goal
import com.viso.domain.usecase.monthsToGoal
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentTeal
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency

@Composable
fun GoalCard(
    goal: Goal,
    onAddAmount: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmountCents > 0) {
        (goal.currentAmountCents.toFloat() / goal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "goalProgress"
    )

    val percentText = "${(progress * 100).toInt()}%"

    val color = when (goal.color) {
        "teal" -> AccentTeal
        "green" -> AccentGreen
        else -> AccentBlue
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(BgCard)
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (goal.isEmergencyFund) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("automático", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentTeal.copy(alpha = 0.18f),
                            selectedLabelColor = AccentTeal
                        )
                    )
                }
            }
            Row {
                if (!goal.isEmergencyFund) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Editar meta", tint = TextSecondary)
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Excluir meta", tint = TextSecondary)
                    }
                }
                IconButton(onClick = onAddAmount) {
                    Icon(Icons.Rounded.Add, contentDescription = "Adicionar valor", tint = color)
                }
            }
        }

        if (goal.isEmergencyFund) {
            Text(
                text = "3× suas contas mensais",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .semantics {
                        progressBarRangeInfo = androidx.compose.ui.semantics.ProgressBarRangeInfo(
                            animatedProgress, 0f..1f
                        )
                    },
                color = color,
                trackColor = color.copy(alpha = 0.15f),
            )
            Text(
                text = percentText,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Guardado",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(goal.currentAmountCents),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Meta",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(goal.targetAmountCents),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        if (goal.monthlyContributionCents > 0) {
            val months = monthsToGoal(goal.targetAmountCents, goal.currentAmountCents, goal.monthlyContributionCents)
            val monthsText = if (months != null && months > 0) " · Completa em $months meses" else ""
            Text(
                text = "Guardando ${formatCurrency(goal.monthlyContributionCents)}/mês$monthsText",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }

        if (progress >= 1f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm)
                    .clip(MaterialTheme.shapes.small)
                    .background(AccentGreen.copy(alpha = 0.1f))
                    .padding(Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.padding(end = Spacing.xs)
                )
                Text(
                    text = if (goal.isEmergencyFund) "Reserva completa! Você está protegido."
                    else "Meta atingida!",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentGreen
                )
            }
        }
    }
}
