package com.viso.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.data.repository.GoalRepository
import com.viso.domain.model.Goal
import com.viso.domain.usecase.CalculateRuleUseCase
import com.viso.domain.usecase.FinancialRule
import com.viso.domain.usecase.emergencyFundTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val rule: FinancialRule = FinancialRule(0, 0, 0, 0),
    val totalMonthlyContributions: Long = 0L,
    val isLoading: Boolean = true,
    val showSheet: Boolean = false,
    val editingGoal: Goal? = null,
    val goalName: String = "",
    val goalTargetCents: Long = 0L,
    val goalContribCents: Long = 0L,
    val goalColor: String = "blue",
    val showAddAmountDialog: Boolean = false,
    val addAmountGoalId: String? = null,
    val addAmountCents: Long = 0L,
    val showDeleteDialog: Boolean = false,
    val deletingGoalId: String? = null
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepo: GoalRepository,
    private val billRepo: BillRepository,
    private val configRepo: ConfigRepository,
    private val extraRepo: ExtraIncomeRepository,
    private val calculateRule: CalculateRuleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val currentMonth = YearMonth.now().toString()
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                goalRepo.getAllGoalsFlow(),
                billRepo.getAllBillsFlow(),
                configRepo.configFlow,
                extraRepo.getTotalForMonthFlow(currentMonth)
            ) { goals, bills, config, extraTotal ->
                val rule = calculateRule(config.effectiveSalaryCents, extraTotal)
                val totalBills = bills.sumOf { it.amountCents }
                val emergencyTarget = emergencyFundTarget(totalBills)

                val updatedGoals = goals.map { goal ->
                    if (goal.isEmergencyFund) {
                        goal.copy(
                            targetAmountCents = emergencyTarget,
                            monthlyContributionCents = rule.savingsCents
                        )
                    } else goal
                }

                // Persist emergency fund updates
                updatedGoals.filter { it.isEmergencyFund }.forEach { goal ->
                    val original = goals.find { it.id == goal.id }
                    if (original != null &&
                        (original.targetAmountCents != goal.targetAmountCents ||
                                original.monthlyContributionCents != goal.monthlyContributionCents)
                    ) {
                        goalRepo.update(goal)
                    }
                }

                val totalContrib = updatedGoals.sumOf { it.monthlyContributionCents }

                GoalsUiState(
                    goals = updatedGoals,
                    rule = rule,
                    totalMonthlyContributions = totalContrib,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.update { current ->
                    state.copy(
                        showSheet = current.showSheet,
                        editingGoal = current.editingGoal,
                        goalName = current.goalName,
                        goalTargetCents = current.goalTargetCents,
                        goalContribCents = current.goalContribCents,
                        goalColor = current.goalColor,
                        showAddAmountDialog = current.showAddAmountDialog,
                        addAmountGoalId = current.addAmountGoalId,
                        addAmountCents = current.addAmountCents,
                        showDeleteDialog = current.showDeleteDialog,
                        deletingGoalId = current.deletingGoalId
                    )
                }
            }
        }
    }

    fun showAddSheet() {
        _uiState.update {
            it.copy(
                showSheet = true, editingGoal = null,
                goalName = "", goalTargetCents = 0L,
                goalContribCents = 0L, goalColor = "blue"
            )
        }
    }

    fun showEditSheet(goal: Goal) {
        _uiState.update {
            it.copy(
                showSheet = true, editingGoal = goal,
                goalName = goal.name,
                goalTargetCents = goal.targetAmountCents,
                goalContribCents = goal.monthlyContributionCents,
                goalColor = goal.color
            )
        }
    }

    fun hideSheet() {
        _uiState.update { it.copy(showSheet = false, editingGoal = null) }
    }

    fun onGoalNameChange(name: String) {
        _uiState.update { it.copy(goalName = name) }
    }

    fun onGoalTargetChange(cents: Long) {
        _uiState.update { it.copy(goalTargetCents = cents) }
    }

    fun onGoalContribChange(cents: Long) {
        _uiState.update { it.copy(goalContribCents = cents) }
    }

    fun onGoalColorChange(color: String) {
        _uiState.update { it.copy(goalColor = color) }
    }

    fun saveGoal() {
        val state = _uiState.value
        if (state.goalName.isBlank()) {
            viewModelScope.launch { _errorEvent.emit("Digite o nome da meta") }
            return
        }
        if (state.goalTargetCents <= 0) {
            viewModelScope.launch { _errorEvent.emit("Digite um valor alvo válido") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val goal = Goal(
                    id = state.editingGoal?.id ?: UUID.randomUUID().toString(),
                    name = state.goalName.trim(),
                    targetAmountCents = state.goalTargetCents,
                    currentAmountCents = state.editingGoal?.currentAmountCents ?: 0L,
                    monthlyContributionCents = state.goalContribCents,
                    isEmergencyFund = false,
                    color = state.goalColor,
                    createdAt = state.editingGoal?.createdAt ?: System.currentTimeMillis()
                )
                if (state.editingGoal != null) {
                    goalRepo.update(goal)
                } else {
                    goalRepo.insert(goal)
                }
                hideSheet()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao salvar meta: ${e.message}")
            }
        }
    }

    fun showAddAmount(goalId: String) {
        _uiState.update {
            it.copy(showAddAmountDialog = true, addAmountGoalId = goalId, addAmountCents = 0L)
        }
    }

    fun hideAddAmount() {
        _uiState.update { it.copy(showAddAmountDialog = false, addAmountGoalId = null) }
    }

    fun onAddAmountChange(cents: Long) {
        _uiState.update { it.copy(addAmountCents = cents) }
    }

    fun confirmAddAmount() {
        val state = _uiState.value
        val goalId = state.addAmountGoalId ?: return
        if (state.addAmountCents <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val goal = goalRepo.getGoalById(goalId) ?: return@launch
                goalRepo.update(goal.copy(currentAmountCents = goal.currentAmountCents + state.addAmountCents))
                hideAddAmount()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao adicionar valor: ${e.message}")
            }
        }
    }

    fun requestDelete(goalId: String) {
        _uiState.update { it.copy(showDeleteDialog = true, deletingGoalId = goalId) }
    }

    fun confirmDelete() {
        val id = _uiState.value.deletingGoalId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                goalRepo.deleteById(id)
                _uiState.update { it.copy(showDeleteDialog = false, deletingGoalId = null) }
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao excluir meta: ${e.message}")
            }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(showDeleteDialog = false, deletingGoalId = null) }
    }
}
