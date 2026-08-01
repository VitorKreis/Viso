package com.viso.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.viso.domain.usecase.FinancialRule
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentTeal

@Composable
fun RuleBar(rule: FinancialRule, modifier: Modifier = Modifier) {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }
    val total = rule.totalCents.coerceAtLeast(1L).toFloat()

    val billsFraction by animateFloatAsState(
        targetValue = if (animate) rule.billsLimitCents / total else 0f,
        animationSpec = tween(800),
        label = "bills"
    )
    val spendingFraction by animateFloatAsState(
        targetValue = if (animate) rule.spendingCents / total else 0f,
        animationSpec = tween(800),
        label = "spending"
    )
    val savingsFraction by animateFloatAsState(
        targetValue = if (animate) rule.savingsCents / total else 0f,
        animationSpec = tween(800),
        label = "savings"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(billsFraction.coerceAtLeast(0.01f))
                .background(AccentBlue)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(spendingFraction.coerceAtLeast(0.01f))
                .background(AccentGreen)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(savingsFraction.coerceAtLeast(0.01f))
                .background(AccentTeal)
        )
    }
}
