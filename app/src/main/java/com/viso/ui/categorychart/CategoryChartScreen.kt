package com.viso.ui.categorychart

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PieChart
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.ui.components.EmptyState
import com.viso.ui.components.PieChart
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChartScreen(
    onBack: () -> Unit,
    viewModel: CategoryChartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgApp,
        topBar = {
            TopAppBar(
                title = { Text("Gastos por Categoria", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgApp)
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            // Show empty state while loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Carregando...", color = TextSecondary)
            }
        } else if (state.categorySpendings.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.PieChart,
                title = "Nenhuma conta cadastrada",
                subtitle = "Adicione contas para ver a distribuição por categoria",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Pie Chart
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(Spacing.lg))
                        PieChart(
                            segments = state.segments,
                            totalAmount = state.totalAmount,
                            centerText = "Total de contas",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(Spacing.lg))
                    }
                }

                // Category list header
                item {
                    Text(
                        text = "Detalhamento",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                // Category items
                items(state.categorySpendings) { spending ->
                    CategoryItem(spending = spending)
                }

                // Total summary
                item {
                    Spacer(Modifier.height(Spacing.md))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgCard, MaterialTheme.shapes.medium)
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = formatCurrency(state.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentBlue
                        )
                    }
                    Spacer(Modifier.height(Spacing.xxxl))
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    spending: com.viso.domain.model.CategorySpending
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, MaterialTheme.shapes.medium)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(spending.color)
            )
            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spending.category.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${String.format("%.1f", spending.percentage)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = formatCurrency(spending.amountCents),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
    }
}
