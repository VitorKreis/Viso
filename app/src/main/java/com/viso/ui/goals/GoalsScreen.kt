package com.viso.ui.goals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.ui.components.CurrencyTextField
import com.viso.ui.components.GoalCard
import com.viso.ui.components.VisoBottomSheet
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentRed
import com.viso.ui.theme.AccentTeal
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.BgInput
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextMuted
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
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
                title = { Text("Metas", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
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
            items(state.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onAddAmount = { viewModel.showAddAmount(goal.id) },
                        onEdit = { viewModel.showEditSheet(goal) },
                        onDelete = if (!goal.isEmergencyFund) {{ viewModel.requestDelete(goal.id) }} else null
                    )
            }

            if (state.goals.size < 3) {
                item {
                    OutlinedCard(
                        onClick = { viewModel.showAddSheet() },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f)),
                        colors = CardDefaults.outlinedCardColors(containerColor = BgApp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xxl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Criar meta", tint = TextMuted, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(Spacing.sm))
                            Text("Criar nova meta", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("máx. 3 metas", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text("Resumo mensal", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Guardando por mês", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(formatCurrency(state.totalMonthlyContributions), style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bloco 10%", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(formatCurrency(state.rule.savingsCents), style = MaterialTheme.typography.titleMedium, color = AccentTeal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        val remaining = state.rule.savingsCents - state.totalMonthlyContributions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Restante livre", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                formatCurrency(remaining.coerceAtLeast(0)),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (remaining >= 0) AccentGreen else AccentRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(Spacing.xxxl)) }
        }

        if (state.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = { Text("Excluir meta?", color = TextPrimary) },
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

        if (state.showAddAmountDialog) {
            val addGoal = state.goals.find { it.id == state.addAmountGoalId }
            VisoBottomSheet(onDismiss = { viewModel.hideAddAmount() }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                        .padding(bottom = Spacing.xxl)
                ) {
                    Text(
                        text = "Adicionar à meta",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (addGoal != null) {
                        Text(
                            text = addGoal.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(Spacing.lg))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgCard, MaterialTheme.shapes.medium)
                                .padding(Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Guardado", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(formatCurrency(addGoal.currentAmountCents), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Após adicionar", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = formatCurrency(addGoal.currentAmountCents + state.addAmountCents),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.lg))

                    CurrencyTextField(
                        amountCents = state.addAmountCents,
                        onAmountChange = { viewModel.onAddAmountChange(it) },
                        label = "Valor a adicionar",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(Spacing.lg))

                    Button(
                        onClick = { viewModel.confirmAddAmount() },
                        enabled = state.addAmountCents > 0,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }

        if (state.showSheet) {
            AddEditGoalSheet(state, viewModel)
        }
    }
}

@Composable
private fun AddEditGoalSheet(state: GoalsUiState, viewModel: GoalsViewModel) {
    VisoBottomSheet(onDismiss = { viewModel.hideSheet() }) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = if (state.editingGoal != null) "Editar meta" else "Nova meta",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = state.goalName,
                onValueChange = { viewModel.onGoalNameChange(it) },
                label = { Text("Nome da meta") },
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
                amountCents = state.goalTargetCents,
                onAmountChange = { viewModel.onGoalTargetChange(it) },
                label = "Valor alvo",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.sm))

            CurrencyTextField(
                amountCents = state.goalContribCents,
                onAmountChange = { viewModel.onGoalContribChange(it) },
                label = "Contribuição mensal",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.md))

            Text("Cor", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                modifier = Modifier.padding(top = Spacing.sm)
            ) {
                listOf("blue" to AccentBlue, "teal" to AccentTeal, "green" to AccentGreen).forEach { (name, color) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (state.goalColor == name) Modifier.border(2.dp, TextPrimary, CircleShape)
                                else Modifier
                            )
                            .clickable { viewModel.onGoalColorChange(name) }
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = { viewModel.saveGoal() },
                enabled = state.goalName.isNotBlank() && state.goalTargetCents > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(if (state.editingGoal != null) "Salvar" else "Criar meta")
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
