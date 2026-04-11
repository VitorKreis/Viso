package com.viso.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.viso.domain.usecase.FinancialRule
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentTeal
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency

@Composable
fun SummaryGrid(rule: FinancialRule, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        SummaryCard(
            label = "Contas",
            percent = "70%",
            value = formatCurrency(rule.billsLimitCents),
            accentColor = AccentBlue,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Gastar",
            percent = "20%",
            value = formatCurrency(rule.spendingCents),
            accentColor = AccentGreen,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Guardar",
            percent = "10%",
            value = formatCurrency(rule.savingsCents),
            accentColor = AccentTeal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    percent: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(BgCard)
            .padding(Spacing.md)
    ) {
        Text(
            text = percent,
            style = MaterialTheme.typography.headlineMedium,
            color = accentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.xs)
        )
    }
}
