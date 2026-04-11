package com.viso.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.GoalRepository
import com.viso.domain.model.Bill
import com.viso.domain.model.Goal
import com.viso.domain.model.SalaryMode
import com.viso.domain.usecase.CalculateRuleUseCase
import com.viso.domain.usecase.emergencyFundTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
    private val billRepo: BillRepository,
    private val goalRepo: GoalRepository,
    private val calculateRule: CalculateRuleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    fun onSalaryChange(cents: Long) {
        _uiState.update {
            val rule = calculateRule(cents, 0L)
            it.copy(
                salaryCents = cents,
                rule = rule
            )
        }
    }

    fun onPaydaySelected(day: Int) {
        _uiState.update { it.copy(payday = day) }
    }

    fun onSalaryModeChange(mode: SalaryMode) {
        _uiState.update {
            val effectiveSalary = if (mode == SalaryMode.SPLIT) it.salary1Cents + it.salary2Cents else it.salaryCents
            val rule = calculateRule(effectiveSalary, 0L)
            it.copy(salaryMode = mode, rule = rule)
        }
    }

    fun onSalary1Change(cents: Long) {
        _uiState.update {
            val effectiveSalary = cents + it.salary2Cents
            val rule = calculateRule(effectiveSalary, 0L)
            it.copy(salary1Cents = cents, rule = rule)
        }
    }

    fun onPayday1Selected(day: Int) {
        _uiState.update { it.copy(payday1 = day, paydayError = day == it.payday2) }
    }

    fun onSalary2Change(cents: Long) {
        _uiState.update {
            val effectiveSalary = it.salary1Cents + cents
            val rule = calculateRule(effectiveSalary, 0L)
            it.copy(salary2Cents = cents, rule = rule)
        }
    }

    fun onPayday2Selected(day: Int) {
        _uiState.update { it.copy(payday2 = day, paydayError = day == it.payday1) }
    }

    fun nextStep() {
        _uiState.update {
            val newStep = (it.step + 1).coerceAtMost(2)
            val effectiveSalary = it.effectiveSalaryCents
            val rule = calculateRule(effectiveSalary, 0L)
            it.copy(step = newStep, rule = rule)
        }
    }

    fun prevStep() {
        _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    fun toggleBillForm() {
        _uiState.update { it.copy(showBillForm = !it.showBillForm) }
    }

    fun onBillNameChange(name: String) {
        _uiState.update { it.copy(billName = name) }
    }

    fun onBillAmountChange(cents: Long) {
        _uiState.update { it.copy(billAmountCents = cents) }
    }

    fun onBillDueDayChange(day: Int) {
        _uiState.update { it.copy(billDueDay = day) }
    }

    fun onBillCategoryChange(cat: String) {
        _uiState.update { it.copy(billCategory = cat) }
    }

    fun addBill() {
        val state = _uiState.value
        if (state.billName.isBlank() || state.billAmountCents <= 0) return
        val bill = Bill(
            id = UUID.randomUUID().toString(),
            name = state.billName,
            amountCents = state.billAmountCents,
            dueDay = state.billDueDay,
            category = state.billCategory,
            isPaid = false,
            paidMonth = "",
            createdAt = System.currentTimeMillis()
        )
        val newBills = state.bills + bill
        val totalBills = newBills.sumOf { it.amountCents }
        _uiState.update {
            it.copy(
                bills = newBills,
                totalBillsCents = totalBills,
                showBillForm = false,
                billName = "",
                billAmountCents = 0L,
                billDueDay = 1,
                billCategory = "outro"
            )
        }
    }

    fun removeBill(billId: String) {
        _uiState.update { state ->
            val newBills = state.bills.filter { it.id != billId }
            state.copy(
                bills = newBills,
                totalBillsCents = newBills.sumOf { it.amountCents }
            )
        }
    }

    fun finishOnboarding(onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val state = _uiState.value
                val effectiveSalary = state.effectiveSalaryCents

                configRepo.updateSalaryMode(state.salaryMode)
                if (state.salaryMode == SalaryMode.SINGLE) {
                    configRepo.updateSalary(state.salaryCents)
                    configRepo.updatePayday(state.payday)
                } else {
                    configRepo.updateSalary1(state.salary1Cents)
                    configRepo.updatePayday1(state.payday1)
                    configRepo.updateSalary2(state.salary2Cents)
                    configRepo.updatePayday2(state.payday2)
                    configRepo.updateSalary(effectiveSalary)
                }
                configRepo.updateLastResetMonth(YearMonth.now().toString())

                state.bills.forEach { bill -> billRepo.insert(bill) }

                val totalBills = state.bills.sumOf { it.amountCents }
                val emergencyTarget = emergencyFundTarget(totalBills)
                val savingsBudget = state.rule.savingsCents
                goalRepo.insert(
                    Goal(
                        id = UUID.randomUUID().toString(),
                        name = "Reserva de emergência",
                        targetAmountCents = emergencyTarget,
                        currentAmountCents = 0L,
                        monthlyContributionCents = savingsBudget,
                        isEmergencyFund = true,
                        color = "teal",
                        createdAt = System.currentTimeMillis()
                    )
                )

                configRepo.setOnboardingDone()
                onDone()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao finalizar configuração: ${e.message}")
            }
        }
    }
}
