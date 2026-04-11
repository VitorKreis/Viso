package com.viso.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.viso.domain.usecase.BillStatus
import com.viso.ui.theme.AccentAmber
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentRed
import com.viso.ui.theme.BadgeLateBg
import com.viso.ui.theme.BadgePaidBg
import com.viso.ui.theme.BadgePendBg
import com.viso.ui.theme.Spacing

@Composable
fun StatusBadge(status: BillStatus, modifier: Modifier = Modifier) {
    val (bg, textColor, label) = when (status) {
        BillStatus.PAID -> Triple(BadgePaidBg, AccentBlue, "pago")
        BillStatus.TODAY -> Triple(BadgeLateBg, AccentRed, "vence hoje")
        BillStatus.UPCOMING -> Triple(BadgePendBg, AccentAmber, "em breve")
        BillStatus.OVERDUE -> Triple(BadgeLateBg, AccentRed, "atrasado")
        BillStatus.FUTURE -> Triple(BadgePendBg, AccentAmber, "pendente")
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}
