package com.viso.ui.bills

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.material3.Switch
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ChipElevation
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.ui.components.BillCard
import com.viso.ui.components.CurrencyTextField
import com.viso.ui.components.EmptyState
import com.viso.ui.components.VisoCategoryPicker
import com.viso.ui.components.VisoBottomSheet
import com.viso.ui.components.VisoNumberPicker
import com.viso.ui.components.VisoYearMonthPicker
import com.viso.ui.theme.AccentAmber
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentRed
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.BgInput
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency
import com.viso.domain.model.RadarLevel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BillsScreen(
    onNavigateToCategoryChart: () -> Unit = {},
    viewModel: BillsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = BgApp,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Contas", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
                actions = {
                    IconButton(onClick = onNavigateToCategoryChart) {
                        Icon(
                            Icons.Rounded.PieChart,
                            contentDescription = "Ver gráfico por categoria",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgApp)
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.showAddSheet() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("+ Nova conta")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item {
                val currentMonth = java.time.YearMonth.now()
                val nextMonth = currentMonth.plusMonths(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    FilterChip(
                        selected = state.selectedMonth == currentMonth.toString(),
                        onClick = { viewModel.setSelectedMonth(currentMonth.toString()) },
                        label = { Text("Mês atual") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.selectedMonth == nextMonth.toString(),
                        onClick = { viewModel.setSelectedMonth(nextMonth.toString()) },
                        label = { Text("Próximo mês") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text("Total ${state.selectedMonth}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(formatCurrency(state.totalBillsCents), style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text("Limite 70%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(formatCurrency(state.rule.billsLimitCents), style = MaterialTheme.typography.titleMedium, color = AccentBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.pendingBillsCents > 0) {
                            AccentAmber.copy(alpha = 0.12f)
                        } else {
                            AccentGreen.copy(alpha = 0.12f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Falta pagar", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                if (state.pendingBillsCount == 0) {
                                    "Nenhuma conta pendente"
                                } else {
                                    "${state.pendingBillsCount} conta(s) ainda abertas"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            formatCurrency(state.pendingBillsCents),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.pendingBillsCents > 0) AccentAmber else AccentGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (!state.isMonthPrepared) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Text("Preparar ${state.currentMonth}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                "Revise suas contas do mes. Ao adicionar uma conta nova ou confirmar a revisao, eu paro os lembretes.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Button(
                                onClick = { viewModel.markMonthPrepared() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                Text("Mes revisado")
                            }
                        }
                    }
                }
            }

            state.radar?.takeIf { it.shouldShow }?.let { radar ->
                item {
                    val radarColor = when (radar.level) {
                        RadarLevel.CRITICAL, RadarLevel.RISK -> AccentRed
                        RadarLevel.ATTENTION -> AccentAmber
                        RadarLevel.OK -> AccentGreen
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = radarColor.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Radar financeiro", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Icon(
                                    if (radar.level == RadarLevel.ATTENTION) Icons.Rounded.Warning else Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = radarColor
                                )
                            }
                            Text(radar.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(radar.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            if (radar.overLimitCents > 0) {
                                Text("Estouro: ${formatCurrency(radar.overLimitCents)}", style = MaterialTheme.typography.titleMedium, color = radarColor)
                            }
                            Text(radar.action, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    FilterChip(
                        selected = state.filter == BillFilter.ALL,
                        onClick = { viewModel.setFilter(BillFilter.ALL) },
                        label = { Text("Todas") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = state.filter == BillFilter.PENDING,
                        onClick = { viewModel.setFilter(BillFilter.PENDING) },
                        label = { Text("Pendentes (${state.pendingBillsCount})") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentAmber,
                            selectedLabelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = state.filter == BillFilter.PAID,
                        onClick = { viewModel.setFilter(BillFilter.PAID) },
                        label = { Text("Pagas (${state.paidBillsCount})") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGreen,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            // Show message when all bills are paid
            if (state.pendingBillsCount == 0 && state.paidBillsCount > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎉",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Spacer(Modifier.height(Spacing.sm))
                            Text(
                                text = "Todas as contas pagas!",
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentGreen
                            )
                            Text(
                                text = "Mês completado com sucesso",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            if (state.bills.isEmpty()) {
                item {
                    val emptyTitle = when (state.filter) {
                        BillFilter.PAID -> "Nenhuma conta paga"
                        BillFilter.PENDING -> "Nenhuma conta pendente"
                        BillFilter.ALL -> "Nenhuma conta cadastrada"
                    }
                    val emptySubtitle = when (state.filter) {
                        BillFilter.PAID -> "As contas pagas aparecerão aqui"
                        BillFilter.PENDING -> "Todas as contas estão pagas! 🎉"
                        BillFilter.ALL -> "Adicione suas contas fixas para começar"
                    }
                    EmptyState(
                        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                        title = emptyTitle,
                        subtitle = emptySubtitle
                    )
                }
            } else {
                state.billsByCategory.forEach { (category, bills) ->
                    stickyHeader {
                        Text(
                            text = category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgApp)
                                .padding(vertical = Spacing.sm)
                        )
                    }
                    items(bills, key = { it.id }) { bill ->
                        BillCard(
                            bill = bill,
                            onPaid = { viewModel.markAsPaid(bill.id) },
                            onDelete = { viewModel.requestDelete(bill.id) },
                            onEdit = { viewModel.showEditSheet(bill) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(Spacing.xxxl)) }
        }

        if (state.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = { Text("Excluir conta?", color = TextPrimary) },
                text = { Text("Esta ação não pode ser desfeita.", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDelete() }) {
                        Text("Excluir", color = AccentRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDelete() }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = BgCard
            )
        }

        if (state.showSheet) {
            AddEditBillSheet(state, viewModel)
        }

        if (state.showMonthPicker) {
            val initialMonth = if (state.isInstallment) state.installmentStartMonth else state.billMonth
            val onConfirm: (String) -> Unit = { month ->
                if (state.isInstallment) {
                    viewModel.onInstallmentStartMonthChange(month)
                } else {
                    viewModel.onBillMonthChange(month)
                }
                viewModel.hideMonthPicker()
            }
            VisoYearMonthPicker(initial = initialMonth, onDismiss = { viewModel.hideMonthPicker() }, onConfirm = onConfirm)
        }
    }
}

@Composable
private fun AddEditBillSheet(state: BillsUiState, viewModel: BillsViewModel) {
    VisoBottomSheet(onDismiss = { viewModel.hideSheet() }) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = if (state.editingBill != null) "Editar conta" else "Nova conta",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = state.billName,
                onValueChange = { viewModel.onBillNameChange(it) },
                label = { Text("Nome") },
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
                amountCents = state.billAmountCents,
                onAmountChange = { viewModel.onBillAmountChange(it) },
                label = "Valor",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.sm))

            VisoNumberPicker(
                value = state.billDueDay,
                onValueChange = { viewModel.onBillDueDayChange(it) },
                range = 1..31,
                label = "Vencimento",
                displayTransform = { "Dia $it" }
            )

            Spacer(Modifier.height(Spacing.sm))
            VisoCategoryPicker(
                selected = state.billCategory,
                onSelect = { viewModel.onBillCategoryChange(it) }
            )

            // Show installment option only for new bills
            if (state.editingBill == null) {
                Spacer(Modifier.height(Spacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("É parcelada?", color = TextPrimary)
                    Switch(checked = state.isInstallment, onCheckedChange = { viewModel.onIsInstallmentChange(it) })
                }
            }

            // Show installment fields
            if (state.isInstallment && state.editingBill == null) {
                Spacer(Modifier.height(Spacing.sm))

                VisoNumberPicker(
                    value = state.totalInstallments,
                    onValueChange = { viewModel.onTotalInstallmentsChange(it) },
                    range = 2..48,
                    label = "Número de parcelas",
                    displayTransform = { "$it vezes" }
                )

                Spacer(Modifier.height(Spacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mês de início", color = TextPrimary)
                    Text(
                        text = try { java.time.YearMonth.parse(state.installmentStartMonth).toString() } catch (_: Exception) { java.time.YearMonth.now().toString() },
                        color = TextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { viewModel.showMonthPicker() }
                    )
                }

                // Preview of installment amount
                val installmentAmount = if (state.totalInstallments > 0) {
                    val baseAmount = state.billAmountCents / state.totalInstallments
                    val remainder = state.billAmountCents % state.totalInstallments
                    baseAmount + remainder
                } else 0L

                if (state.billAmountCents > 0) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "${state.totalInstallments}x de ${formatCurrency(installmentAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentBlue
                    )
                    Text(
                        text = "Total: ${formatCurrency(state.billAmountCents)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            } else {
                Spacer(Modifier.height(Spacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recorrente mensal", color = TextPrimary)
                    Switch(checked = state.billIsRecurring, onCheckedChange = { viewModel.onBillIsRecurringChange(it) })
                }

                Spacer(Modifier.height(Spacing.sm))
                val currentMonth = java.time.YearMonth.now()
                val nextMonth = currentMonth.plusMonths(1)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mês da conta", color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = try { java.time.YearMonth.parse(state.billMonth).toString() } catch (_: Exception) { java.time.YearMonth.now().toString() },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { viewModel.showMonthPicker() }
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    FilterChip(
                        selected = state.billMonth == currentMonth.toString(),
                        onClick = { viewModel.onBillMonthChange(currentMonth.toString()) },
                        label = { Text("Mês atual") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.billMonth == nextMonth.toString(),
                        onClick = { viewModel.onBillMonthChange(nextMonth.toString()) },
                        label = { Text("Próximo mês") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.editingBill == null && state.billAmountCents > 0) {
                val projectedTotal = state.totalBillsCents + state.billAmountCents
                val projectedOverLimit = projectedTotal - state.rule.billsLimitCents
                if (projectedOverLimit > 0) {
                    Spacer(Modifier.height(Spacing.sm))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                "Essa conta passa do limite",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                "Depois dela, suas contas ficam ${formatCurrency(projectedOverLimit)} acima do bloco de 70%.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = { viewModel.saveBill() },
                enabled = state.billName.isNotBlank() && state.billAmountCents > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(if (state.editingBill != null) "Salvar" else "Adicionar")
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
