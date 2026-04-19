package com.viso.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.db.VisoDB
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.data.repository.GoalRepository
import com.viso.data.repository.BillRepository
import com.viso.domain.model.Config
import com.viso.domain.model.ExtraIncome
import com.viso.domain.model.SalaryMode
import com.viso.domain.usecase.ScheduleNotificationsUseCase
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

data class ConfigUiState(
    val config: Config = Config(),
    val salaryCents: Long = 0L,
    val payday: Int = 5,
    val salaryMode: SalaryMode = SalaryMode.SINGLE,
    val salary1Cents: Long = 0L,
    val payday1: Int = 5,
    val salary2Cents: Long = 0L,
    val payday2: Int = 20,
    val paydayError: Boolean = false,
    val extras: List<ExtraIncome> = emptyList(),
    val notifEnabled: Boolean = true,
    val notifDaysBefore: Int = 3,
    val showExtraSheet: Boolean = false,
    val extraName: String = "",
    val extraAmountCents: Long = 0L,
    val showResetDialog1: Boolean = false,
    val showResetDialog2: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
    private val extraRepo: ExtraIncomeRepository,
    private val scheduleNotif: ScheduleNotificationsUseCase,
    private val db: VisoDB,
    private val goalRepo: GoalRepository,
    private val billRepo: BillRepository,
    private val calculateRule: com.viso.domain.usecase.CalculateRuleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    private val _postResetEvent = MutableSharedFlow<Unit>(replay = 0)
    val postResetEvent: SharedFlow<Unit> = _postResetEvent.asSharedFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val currentMonth = YearMonth.now().toString()
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                configRepo.configFlow,
                extraRepo.getByMonthFlow(currentMonth)
            ) { config, extras ->
                ConfigUiState(
                    config = config,
                    salaryCents = config.salaryCents,
                    payday = config.payday,
                    salaryMode = config.salaryMode,
                    salary1Cents = config.salary1Cents,
                    payday1 = config.payday1,
                    salary2Cents = config.salary2Cents,
                    payday2 = config.payday2,
                    paydayError = config.payday1 == config.payday2,
                    extras = extras,
                    notifEnabled = config.notifDaysBefore > 0,
                    notifDaysBefore = config.notifDaysBefore.coerceAtLeast(1),
                    isLoading = false
                )
            }.collect { state ->
                _uiState.update { current ->
                    state.copy(
                        showExtraSheet = current.showExtraSheet,
                        extraName = current.extraName,
                        extraAmountCents = current.extraAmountCents,
                        showResetDialog1 = current.showResetDialog1,
                        showResetDialog2 = current.showResetDialog2
                    )
                }
            }
        }
    }

    fun onSalaryChange(cents: Long) {
        _uiState.update { it.copy(salaryCents = cents) }
    }

    fun onPaydayChange(day: Int) {
        _uiState.update { it.copy(payday = day) }
    }

    fun onSalaryModeChange(mode: SalaryMode) {
        _uiState.update { it.copy(salaryMode = mode) }
    }

    fun onSalary1Change(cents: Long) {
        _uiState.update { it.copy(salary1Cents = cents) }
    }

    fun onPayday1Change(day: Int) {
        _uiState.update { it.copy(payday1 = day, paydayError = day == it.payday2) }
    }

    fun onSalary2Change(cents: Long) {
        _uiState.update { it.copy(salary2Cents = cents) }
    }

    fun onPayday2Change(day: Int) {
        _uiState.update { it.copy(payday2 = day, paydayError = day == it.payday1) }
    }

    fun saveSalary() {
        val state = _uiState.value
        if (state.salaryMode == SalaryMode.SPLIT && state.payday1 == state.payday2) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                configRepo.updateSalaryMode(state.salaryMode)
                if (state.salaryMode == SalaryMode.SINGLE) {
                    configRepo.updateSalary(state.salaryCents)
                    configRepo.updatePayday(state.payday)
                } else {
                    configRepo.updateSalary1(state.salary1Cents)
                    configRepo.updatePayday1(state.payday1)
                    configRepo.updateSalary2(state.salary2Cents)
                    configRepo.updatePayday2(state.payday2)
                    configRepo.updateSalary(state.salary1Cents + state.salary2Cents)
                }
                scheduleNotif()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao salvar salário: ${e.message}")
            }
        }
    }

    fun onNotifToggle(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (enabled) {
                    configRepo.updateNotifDaysBefore(_uiState.value.notifDaysBefore)
                } else {
                    configRepo.updateNotifDaysBefore(0)
                }
                scheduleNotif()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao atualizar notificações: ${e.message}")
            }
        }
    }

    fun onNotifDaysChange(days: Int) {
        _uiState.update { it.copy(notifDaysBefore = days) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                configRepo.updateNotifDaysBefore(days)
                scheduleNotif()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao atualizar dias de notificação: ${e.message}")
            }
        }
    }

    fun showExtraSheet() {
        _uiState.update { it.copy(showExtraSheet = true, extraName = "", extraAmountCents = 0L) }
    }

    fun hideExtraSheet() {
        _uiState.update { it.copy(showExtraSheet = false) }
    }

    fun onExtraNameChange(name: String) {
        _uiState.update { it.copy(extraName = name) }
    }

    fun onExtraAmountChange(cents: Long) {
        _uiState.update { it.copy(extraAmountCents = cents) }
    }

    fun addExtra() {
        val state = _uiState.value
        if (state.extraName.isBlank() || state.extraAmountCents <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                extraRepo.insert(
                    ExtraIncome(
                        id = UUID.randomUUID().toString(),
                        name = state.extraName,
                        amountCents = state.extraAmountCents,
                        month = YearMonth.now().toString()
                    )
                )
                hideExtraSheet()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao adicionar entrada: ${e.message}")
            }
        }
    }

    fun deleteExtra(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                extraRepo.deleteById(id)
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao excluir entrada: ${e.message}")
            }
        }
    }

    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog1 = true) }
    }

    fun confirmReset1() {
        _uiState.update { it.copy(showResetDialog1 = false, showResetDialog2 = true) }
    }

    fun confirmReset2() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.clearAllTables()
                configRepo.clearAll()
                _uiState.update { it.copy(showResetDialog2 = false) }
                // Notify UI to prompt user to recreate emergency fund
                _postResetEvent.emit(Unit)
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao resetar dados: ${e.message}")
            }
        }
    }

    fun createEmergencyFund() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentMonth = YearMonth.now().toString()
                val bills = try { billRepo.getAllBills() } catch (t: Throwable) { emptyList<com.viso.domain.model.Bill>() }
                val totalBills = bills.sumOf { it.amountCents }
                val emergencyTarget = com.viso.domain.usecase.emergencyFundTarget(totalBills)
                val extraTotal = try { extraRepo.getTotalForMonth(currentMonth) } catch (t: Throwable) { 0L }
                val config = try { configRepo.getConfig() } catch (t: Throwable) { com.viso.domain.model.Config() }
                val rule = calculateRule(config.effectiveSalaryCents, extraTotal)
                val savingsBudget = rule.savingsCents

                goalRepo.insert(
                    com.viso.domain.model.Goal(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Reserva de emergência",
                        targetAmountCents = emergencyTarget,
                        currentAmountCents = 0L,
                        monthlyContributionCents = savingsBudget,
                        isEmergencyFund = true,
                        color = "teal",
                        createdAt = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao criar reserva de emergência: ${e.message}")
            }
        }
    }

    fun cancelReset() {
        _uiState.update { it.copy(showResetDialog1 = false, showResetDialog2 = false) }
    }
}
