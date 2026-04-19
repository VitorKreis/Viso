package com.viso.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing

@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = BgApp,
        topBar = {
            TopAppBar(
                title = { Text("Relatórios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgApp)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(Spacing.lg)) {
            Text(text = state.indicator, style = MaterialTheme.typography.bodyMedium)

            // Filters
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterButton(label = "Consolidado", selected = state.filter == ReportFilter.ALL) { viewModel.setFilter(ReportFilter.ALL) }
                FilterButton(label = "Contas", selected = state.filter == ReportFilter.BILLS) { viewModel.setFilter(ReportFilter.BILLS) }
                FilterButton(label = "Gastos", selected = state.filter == ReportFilter.SPENDING) { viewModel.setFilter(ReportFilter.SPENDING) }
                FilterButton(label = "Poupança", selected = state.filter == ReportFilter.SAVINGS) { viewModel.setFilter(ReportFilter.SAVINGS) }
            }

            // Simple bar chart substitute
            val months = state.months
            val values = remember(months, state.filter) {
                months.map {
                    when (state.filter) {
                        ReportFilter.ALL -> it.billsCents + it.spendingCents + it.savingsCents
                        ReportFilter.BILLS -> it.billsCents
                        ReportFilter.SPENDING -> it.spendingCents
                        ReportFilter.SAVINGS -> it.savingsCents
                    }
                }
            }

            val max = (values.maxOrNull() ?: 1L).coerceAtLeast(1L)

            Row(modifier = Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                months.forEachIndexed { idx, m ->
                    val v = values.getOrNull(idx) ?: 0L
                    val heightPct = v.toFloat() / max.toFloat()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(modifier = Modifier
                            .width(20.dp)
                            .height((160 * heightPct).dp)
                            .background(color = BgCard))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = if (selected) MaterialTheme.colorScheme.primary else BgCard)) {
        Text(label)
    }
}
