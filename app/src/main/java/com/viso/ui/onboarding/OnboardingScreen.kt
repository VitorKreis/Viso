package com.viso.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.domain.model.SalaryMode
import com.viso.ui.components.CurrencyTextField
import com.viso.ui.components.RuleBar
import com.viso.ui.components.SummaryGrid
import com.viso.ui.components.VisoCategoryPicker
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

val categories = listOf("moradia", "alimentacao", "transporte", "saude", "educacao", "utilidade", "lazer", "outro")

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.lg)
        ) {
        LinearProgressIndicator(
            progress = { (state.step + 1) / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xxl),
            color = AccentBlue,
            trackColor = AccentBlue.copy(alpha = 0.15f),
        )

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            },
            label = "onboardingStep"
        ) { step ->
            when (step) {
                0 -> SalaryStep(state, viewModel)
                1 -> BillsStep(state, viewModel)
                2 -> SummaryStep(state, viewModel, onFinish)
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalaryStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Qual é o seu salário mensal?",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = "Como você recebe seu salário?",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
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

        Spacer(Modifier.height(Spacing.lg))

        AnimatedVisibility(visible = state.salaryMode == SalaryMode.SINGLE) {
            Column {
                CurrencyTextField(
                    amountCents = state.salaryCents,
                    onAmountChange = { viewModel.onSalaryChange(it) },
                    label = "Salário",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spacing.lg))

                Text(
                    text = "Qual dia do mês você recebe?",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(Spacing.sm))

                VisoNumberPicker(
                    value = state.payday,
                    onValueChange = { viewModel.onPaydaySelected(it) },
                    range = 1..31,
                    label = "Dia do recebimento",
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
                        onValueChange = { viewModel.onPayday1Selected(it) },
                        range = 1..31,
                        label = "Recebimento 1",
                        displayTransform = { "Dia $it" },
                        modifier = Modifier.weight(1f)
                    )
                    VisoNumberPicker(
                        value = state.payday2,
                        onValueChange = { viewModel.onPayday2Selected(it) },
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
        Text(
            text = "Esse valor será a base para calcular seus limites de gasto.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { viewModel.nextStep() },
            enabled = state.effectiveSalaryCents > 0 && !state.paydayError,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text("Continuar")
        }
    }
}

@Composable
private fun BillsStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Suas contas fixas",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Adicione as contas que você paga todo mês",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(Spacing.lg))

        if (state.bills.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total: ${formatCurrency(state.totalBillsCents)}", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("Limite 70%: ${formatCurrency(state.rule.billsLimitCents)}", color = AccentBlue, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(state.bills, key = { it.id }) { bill ->
                Card(colors = CardDefaults.cardColors(containerColor = BgCard)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bill.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Dia ${bill.dueDay} · ${bill.category}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatCurrency(bill.amountCents), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            IconButton(onClick = { viewModel.removeBill(bill.id) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remover", tint = AccentRed)
                            }
                        }
                    }
                }
            }

            if (state.showBillForm) {
                item {
                    BillFormInline(state, viewModel)
                }
            }

            item {
                Button(
                    onClick = { viewModel.toggleBillForm() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.15f))
                ) {
                    Text(if (state.showBillForm) "Cancelar" else "+ Adicionar conta", color = AccentBlue)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { viewModel.prevStep() }) {
                Text("Voltar", color = TextSecondary)
            }
            if (state.bills.isEmpty()) {
                TextButton(onClick = { viewModel.nextStep() }) {
                    Text("Pular esta etapa", color = TextSecondary)
                }
            } else {
                Button(
                    onClick = { viewModel.nextStep() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

@Composable
private fun BillFormInline(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            OutlinedTextField(
                value = state.billName,
                onValueChange = { viewModel.onBillNameChange(it) },
                label = { Text("Nome da conta") },
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
                range = 1..28,
                label = "Vencimento",
                displayTransform = { "Dia $it" }
            )

            Spacer(Modifier.height(Spacing.sm))
            VisoCategoryPicker(
                selected = state.billCategory,
                onSelect = { viewModel.onBillCategoryChange(it) }
            )

            Spacer(Modifier.height(Spacing.md))
            Button(
                onClick = { viewModel.addBill() },
                enabled = state.billName.isNotBlank() && state.billAmountCents > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Adicionar")
            }
        }
    }
}

@Composable
private fun SummaryStep(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tudo pronto!",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Veja como seu dinheiro será distribuído",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(Spacing.xxl))

        Text(
            text = formatCurrency(state.effectiveSalaryCents),
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary
        )
        Text(
            text = "Salário mensal",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(Spacing.xxl))
        SummaryGrid(rule = state.rule)
        Spacer(Modifier.height(Spacing.md))
        RuleBar(rule = state.rule)

        if (state.bills.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.xxl))
            Card(colors = CardDefaults.cardColors(containerColor = BgCard)) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text("${state.bills.size} contas cadastradas", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Total: ${formatCurrency(state.totalBillsCents)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { viewModel.prevStep() }) {
                Text("Voltar", color = TextSecondary)
            }
            Button(
                onClick = { viewModel.finishOnboarding(onFinish) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Entrar no Viso")
            }
        }
    }
}
