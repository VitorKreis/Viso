package com.viso.ui.config

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.domain.model.SalaryMode
import com.viso.ui.components.CurrencyTextField
import com.viso.ui.components.VisoBottomSheet
import com.viso.ui.components.VisoNumberPicker
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentRed
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.BgInput
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onBack: () -> Unit,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = { Text("Configurações", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
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
            // Salary section
            item {
                Text("Salário", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(Spacing.sm))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = state.salaryMode == SalaryMode.SINGLE,
                        onClick = { viewModel.onSalaryModeChange(SalaryMode.SINGLE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Parcela única")
                    }
                    SegmentedButton(
                        selected = state.salaryMode == SalaryMode.SPLIT,
                        onClick = { viewModel.onSalaryModeChange(SalaryMode.SPLIT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Duas parcelas")
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                AnimatedVisibility(visible = state.salaryMode == SalaryMode.SINGLE) {
                    Column {
                        CurrencyTextField(
                            amountCents = state.salaryCents,
                            onAmountChange = { viewModel.onSalaryChange(it) },
                            label = "Valor mensal"
                        )
                        Spacer(Modifier.height(Spacing.sm))

                        VisoNumberPicker(
                            value = state.payday,
                            onValueChange = { viewModel.onPaydayChange(it) },
                            range = 1..31,
                            label = "Dia de pagamento",
                            displayTransform = { "Dia $it" }
                        )
                    }
                }

                AnimatedVisibility(visible = state.salaryMode == SalaryMode.SPLIT) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            CurrencyTextField(
                                amountCents = state.salary1Cents,
                                onAmountChange = { viewModel.onSalary1Change(it) },
                                label = "Parcela 1",
                                modifier = Modifier.weight(1f)
                            )
                            CurrencyTextField(
                                amountCents = state.salary2Cents,
                                onAmountChange = { viewModel.onSalary2Change(it) },
                                label = "Parcela 2",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            VisoNumberPicker(
                                value = state.payday1,
                                onValueChange = { viewModel.onPayday1Change(it) },
                                range = 1..31,
                                label = "Recebimento 1",
                                displayTransform = { "Dia $it" },
                                modifier = Modifier.weight(1f)
                            )
                            VisoNumberPicker(
                                value = state.payday2,
                                onValueChange = { viewModel.onPayday2Change(it) },
                                range = 1..31,
                                label = "Recebimento 2",
                                displayTransform = { "Dia $it" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (state.paydayError) {
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                "Os dias de recebimento devem ser diferentes",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentRed
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.sm))
                Button(
                    onClick = { viewModel.saveSalary() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.paydayError,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Salvar")
                }
            }

            item { HorizontalDivider(color = BgCard) }

            // Extra incomes
            item {
                Text("Entradas extras", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("do mês atual", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            items(state.extras, key = { it.id }) { extra ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, MaterialTheme.shapes.medium)
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(extra.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(formatCurrency(extra.amountCents), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    IconButton(onClick = { viewModel.deleteExtra(extra.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Excluir", tint = AccentRed)
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.showExtraSheet() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.15f))
                ) {
                    Text("+ Adicionar entrada", color = AccentBlue)
                }
            }

            item { HorizontalDivider(color = BgCard) }

            // Notifications
            item {
                Text("Notificações", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ativar notificações", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Switch(
                        checked = state.notifEnabled,
                        onCheckedChange = { viewModel.onNotifToggle(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = AccentBlue)
                    )
                }
                if (state.notifEnabled) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "Avisar ${state.notifDaysBefore} dias antes do vencimento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    VisoNumberPicker(
                        value = state.notifDaysBefore,
                        onValueChange = { viewModel.onNotifDaysChange(it) },
                        range = 1..7,
                        label = "Dias de antecedência",
                        displayTransform = { "$it dia${if (it > 1) "s" else ""}" }
                    )
                }
            }

            item { HorizontalDivider(color = BgCard) }

            // Data reset
            item {
                Text("Dados", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(Spacing.sm))
                Button(
                    onClick = { viewModel.showResetDialog() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.15f))
                ) {
                    Text("Resetar todos os dados", color = AccentRed)
                }
            }

            item { HorizontalDivider(color = BgCard) }

            // About
            item {
                Text("Sobre", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(Spacing.xs))
                Text("Viso v1.0.0", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(
                    "Organize suas finanças com a regra 70-20-10: 70% para contas, 20% para gastar e 10% para guardar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(Spacing.xxxl))
            }
        }

        if (state.showResetDialog1) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelReset() },
                title = { Text("Resetar todos os dados?", color = TextPrimary) },
                text = {
                    Text(
                        "Isso apagará todas as contas, metas e configurações. Não é reversível.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmReset1() }) {
                        Text("Continuar", color = AccentRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelReset() }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = BgCard
            )
        }

        if (state.showResetDialog2) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelReset() },
                title = { Text("Tem certeza?", color = TextPrimary) },
                text = {
                    Text(
                        "Todos os dados serão perdidos permanentemente.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmReset2() }) {
                        Text("Sim, resetar", color = AccentRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelReset() }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = BgCard
            )
        }

        if (state.showExtraSheet) {
            VisoBottomSheet(onDismiss = { viewModel.hideExtraSheet() }) {
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
