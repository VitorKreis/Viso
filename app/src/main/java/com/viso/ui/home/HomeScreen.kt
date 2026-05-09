package com.viso.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.domain.model.SalaryMode
import com.viso.domain.model.SalaryPart
import com.viso.ui.components.BillCard
import com.viso.ui.components.CurrencyTextField
import com.viso.ui.components.EmptyState
import com.viso.ui.components.RuleBar
import com.viso.ui.components.SummaryGrid
import com.viso.ui.components.VisoBottomSheet
import com.viso.ui.theme.AccentAmber
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentRed
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.BgInput
import com.viso.ui.theme.BorderSubtle
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToConfig: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToStreaks: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val now = LocalDate.now()
    val greeting = when (now.atStartOfDay().hour) {
        in 0..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
    val monthName = YearMonth.now().month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        .replaceFirstChar { it.uppercase() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BgApp,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(greeting, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(monthName, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToReports) {
                        Icon(Icons.Rounded.Savings, contentDescription = "Relatórios", tint = TextSecondary)
                    }
                    IconButton(onClick = onNavigateToConfig) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Configurações", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgApp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddExtra() },
                containerColor = AccentBlue,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Adicionar entrada extra")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Block 1 - Balance
            if (state.config.salaryMode == SalaryMode.SPLIT && state.part1 != null && state.part2 != null) {
                item {
                    SplitSalaryCards(
                        part1 = state.part1!!,
                        part2 = state.part2!!,
                        totalCents = state.rule.totalCents,
                        spendingCents = state.rule.spendingCents,
                        extraTotalCents = state.extraTotalCents
                    )
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            Text("Salário do mês", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = formatCurrency(state.rule.totalCents),
                                style = MaterialTheme.typography.displayLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Disponível para gastar: ${formatCurrency(state.rule.spendingCents)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (state.extraTotalCents > 0) {
                                Text(
                                    text = "Inclui ${formatCurrency(state.extraTotalCents)} em entradas extras",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AccentGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Block 1.5 - Streak Card (if has streak)
            if (state.streakInfo?.currentStreak ?: 0 > 0) {
                item {
                    StreakSummaryCard(
                        streak = state.streakInfo?.currentStreak ?: 0,
                        maxStreak = state.streakInfo?.maxStreak ?: 0,
                        onClick = onNavigateToStreaks
                    )
                }
            }

            // Block 2 - Rule 70-20-10
            item {
                Column {
                    SummaryGrid(rule = state.rule)
                    Spacer(Modifier.height(Spacing.sm))
                    RuleBar(rule = state.rule)
                }
            }

            // Block 3 - Bills status
            item {
                val billsPercent = if (state.rule.totalCents > 0) {
                    (state.totalBillsCents * 100) / state.rule.totalCents
                } else 0L

                val (marginColor, marginIcon) = when {
                    billsPercent > 80 -> AccentRed to Icons.Rounded.Error
                    billsPercent > 70 -> AccentAmber to Icons.Rounded.Warning
                    else -> AccentGreen to null
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total de contas", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(formatCurrency(state.totalBillsCents), style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Limite 70%", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(formatCurrency(state.rule.billsLimitCents), style = MaterialTheme.typography.titleMedium, color = AccentBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text("Margem", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                if (marginIcon != null) {
                                    Icon(marginIcon, contentDescription = null, tint = marginColor, modifier = Modifier)
                                }
                            }
                            Text(formatCurrency(state.margin), style = MaterialTheme.typography.titleMedium, color = marginColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Block 4 - Upcoming bills
            item {
                Text("Próximos vencimentos", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text("nos próximos 7 dias", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            if (state.upcomingBills.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.CheckCircle,
                        title = "Semana tranquila",
                        subtitle = "Nenhum vencimento nos próximos 7 dias"
                    )
                }
            } else {
                items(state.upcomingBills, key = { it.id }) { bill ->
                    BillCard(
                        bill = bill,
                        onPaid = {},
                        onDelete = {},
                        onEdit = {}
                    )
                }
            }

            item { Spacer(Modifier.height(Spacing.xxxl)) }
        }

        if (state.showAddExtraSheet) {
            VisoBottomSheet(onDismiss = { viewModel.hideAddExtra() }) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text("Adicionar entrada extra", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Spacer(Modifier.height(Spacing.lg))
                    OutlinedTextField(
                        value = state.extraName,
                        onValueChange = { viewModel.onExtraNameChange(it) },
                        label = { Text("Descrição") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BgInput,
                            focusedContainerColor = BgInput,
                            unfocusedContainerColor = BgInput,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    CurrencyTextField(
                        amountCents = state.extraAmountCents,
                        onAmountChange = { viewModel.onExtraAmountChange(it) },
                        label = "Valor"
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Button(
                        onClick = { viewModel.addExtra() },
                        enabled = state.extraName.isNotBlank() && state.extraAmountCents > 0,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Adicionar")
                    }
                    Spacer(Modifier.height(Spacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun SplitSalaryCards(
    part1: SalaryPart,
    part2: SalaryPart,
    totalCents: Long,
    spendingCents: Long,
    extraTotalCents: Long
) {
    val today = LocalDate.now().dayOfMonth
    val nextPayday = if (today <= part1.payday) part1.payday
    else if (today <= part2.payday) part2.payday
    else part1.payday // next month

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SalaryPartCard(
                label = "Parcela 1",
                part = part1,
                isNext = part1.payday == nextPayday,
                modifier = Modifier.weight(1f)
            )
            SalaryPartCard(
                label = "Parcela 2",
                part = part2,
                isNext = part2.payday == nextPayday,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "Total do mês: ${formatCurrency(totalCents)}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = "Disponível para gastar: ${formatCurrency(spendingCents)}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        if (extraTotalCents > 0) {
            Text(
                text = "Inclui ${formatCurrency(extraTotalCents)} em entradas extras",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentGreen
            )
        }
    }
}

@Composable
private fun SalaryPartCard(
    label: String,
    part: SalaryPart,
    isNext: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = BgCard),
        border = BorderStroke(
            width = if (isNext) 1.5.dp else 1.dp,
            color = if (isNext) AccentBlue else BorderSubtle
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                "Dia ${part.payday}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNext) AccentBlue else TextSecondary
            )
            Text(
                formatCurrency(part.amountCents),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Contas: ${formatCurrency(part.totalAssignedCents)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Sobra: ${formatCurrency(part.remainingCents)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (part.remainingCents >= 0) AccentGreen else AccentRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StreakSummaryCard(
    streak: Int,
    maxStreak: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fireIcon = when {
        streak >= 24 -> "👑"
        streak >= 12 -> "🔥🔥🔥"
        streak >= 6 -> "🔥🔥"
        streak >= 3 -> "🔥"
        else -> "🔥"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.1f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = fireIcon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        text = "$streak ${if (streak == 1) "mês" else "meses"} em dia!",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Recorde: $maxStreak • Toque para ver conquistas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.Savings,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
