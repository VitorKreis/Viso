package com.viso.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency

@Composable
fun PieChart(
    segments: List<PieSegment>,
    totalAmount: Long,
    modifier: Modifier = Modifier,
    centerText: String? = null,
    showTotalInCenter: Boolean = true
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(segments) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            val totalValue = segments.sumOf { it.value.toDouble() }.toFloat()
            if (totalValue == 0f) return@Canvas

            var startAngle = -90f
            val strokeWidth = 50f

            segments.forEach { segment ->
                val sweepAngle = (segment.value / totalValue) * 360f * animatedProgress.value

                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                startAngle += sweepAngle
            }
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showTotalInCenter) {
                Text(
                    text = formatCurrency(totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                if (centerText != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = centerText,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (centerText != null) {
                Text(
                    text = centerText,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class PieSegment(
    val value: Float,
    val color: Color,
    val label: String
)
