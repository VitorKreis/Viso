package com.viso.ui.evolution

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.ui.components.EmptyState
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentRed
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextMuted
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvolutionScreen(
    onBack: () -> Unit,
    viewModel: EvolutionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgApp,
        topBar = {
            TopAppBar(
                title = { Text("Evolução Mensal", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
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
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (state.isLoading) {
                item { Text("Carregando...", color = TextSecondary) }
                return@LazyColumn
            }

            if (state.months.isEmpty() || state.months.all { it.totalCents == 0L }) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.TrendingDown,
                        title = "Nenhum gasto registrado",
                        subtitle = "Os dados aparecerão aqui conforme você marcar contas como pagas."
                    )
                }
                return@LazyColumn
            }

            // Period selector
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = state.selectedPeriod == 6,
                        onClick = { viewModel.onPeriodChange(6) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("6 meses") }
                    SegmentedButton(
                        selected = state.selectedPeriod == 12,
                        onClick = { viewModel.onPeriodChange(12) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("12 meses") }
                }
                Spacer(Modifier.height(Spacing.md))
            }

            // Line chart (custom Canvas)
            item {
                val lineColor = AccentBlue
                val fillColor = AccentBlue.copy(alpha = 0.12f)
                val months = state.months
                val maxVal = months.maxOf { it.totalCents }.coerceAtLeast(1)
                val density = LocalDensity.current

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val chartWidth = size.width
                        val chartHeight = size.height - 40f
                        val stepX = chartWidth / (months.size - 1).coerceAtLeast(1)
                        val points = months.mapIndexed { index, spending ->
                            val x = index * stepX
                            val y = chartHeight - (spending.totalCents.toFloat() / maxVal * chartHeight)
                            Offset(x, y + 20f)
                        }

                        // Grid lines
                        for (i in 0..4) {
                            val y = chartHeight * i / 4 + 20f
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(chartWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        // Fill area under line
                        if (points.size >= 2) {
                            val fillPath = Path().apply {
                                moveTo(points.first().x, chartHeight + 20f)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, chartHeight + 20f)
                                close()
                            }
                            drawPath(fillPath, fillColor)
                        }

                        // Line
                        if (points.size >= 2) {
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            drawPath(linePath, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }

                        // Dots
                        points.forEach { point ->
                            drawCircle(lineColor, 5f, point)
                            drawCircle(Color.White, 3f, point)
                        }

                        // Labels
                        val textPaint = android.graphics.Paint().apply {
                            color = 0xFF4A6380.toInt()
                            textSize = 10f * density.density
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        months.forEachIndexed { index, spending ->
                            if (index % 2 == 0 || months.size <= 6) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    spending.label,
                                    points[index].x,
                                    size.height - 5f,
                                    textPaint
                                )
                            }
                        }
                    }
                }
            }

            // Divider
            item { HorizontalDivider(color = BgCard) }

            // Per-month breakdown
            item {
                Text("Detalhamento Mensal", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(Spacing.sm))
            }

            items(state.months.reversed()) { month ->
                val idx = state.months.indexOf(month)
                val prevAmount = state.months.getOrNull(idx - 1)?.totalCents ?: month.totalCents
                val diff = month.totalCents - prevAmount
                val isUp = diff > 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, MaterialTheme.shapes.medium)
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(month.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            text = if (diff != 0L) {
                                if (isUp) "↑ ${formatCurrency(diff)}" else "↓ ${formatCurrency(-diff)}"
                            } else "—",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUp) AccentRed else AccentGreen
                        )
                    }
                    Text(
                        text = formatCurrency(month.totalCents),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            item { Spacer(Modifier.height(Spacing.xxl)) }
        }
    }
}
