package com.viso.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.domain.model.Bill
import com.viso.domain.model.Config
import com.viso.domain.model.ExtraIncome
import com.viso.domain.model.SalaryMode
import com.viso.domain.model.SalaryPart
import com.viso.domain.model.StreakInfo
import com.viso.domain.model.distributeBillsByPart
import com.viso.domain.usecase.CalculateRuleUseCase
import com.viso.domain.usecase.CalculateStreaksUseCase
import com.viso.domain.usecase.FinancialRule
import com.viso.domain.usecase.MonthlyResetUseCase
import com.viso.domain.usecase.billsMargin
import com.viso.domain.usecase.getBillStatus
import com.viso.domain.usecase.BillStatus
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
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

data class HomeUiState(
    val config: Config = Config(),
    val rule: FinancialRule = FinancialRule(0, 0, 0, 0),
    val bills: List<Bill> = emptyList(),
    val extras: List<ExtraIncome> = emptyList(),
    val upcomingBills: List<Bill> = emptyList(),
    val totalBillsCents: Long = 0L,
    val margin: Long = 0L,
    val extraTotalCents: Long = 0L,
    val isLoading: Boolean = true,
    val showAddExtraSheet: Boolean = false,
    val extraName: String = "",
    val extraAmountCents: Long = 0L,
    val part1: SalaryPart? = null,
    val part2: SalaryPart? = null,
    val streakInfo: StreakInfo? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
    private val billRepo: BillRepository,
    private val extraRepo: ExtraIncomeRepository,
    private val resetUseCase: MonthlyResetUseCase,
    private val calculateRule: CalculateRuleUseCase,
    private val calculateStreaks: CalculateStreaksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            resetUseCase()
            loadData()
        }
    }

    private fun loadData() {
        val currentMonth = YearMonth.now().toString()
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                configRepo.configFlow,
                billRepo.getAllBillsFlow(),
                extraRepo.getByMonthFlow(currentMonth),
                calculateStreaks()
            ) { config, bills, extras, streakInfo ->
                val extraTotal = extras.sumOf { it.amountCents }
                val effectiveSalary = config.effectiveSalaryCents
                val rule = calculateRule(effectiveSalary, extraTotal)
                val totalBills = bills.sumOf { it.amountCents }
                val margin = billsMargin(rule.billsLimitCents, totalBills)
                val today = LocalDate.now()
                val upcoming = bills.filter { bill ->
                    val status = getBillStatus(bill, today)
                    status == BillStatus.TODAY || status == BillStatus.UPCOMING || status == BillStatus.OVERDUE
                }

                var part1: SalaryPart? = null
                var part2: SalaryPart? = null
                if (config.salaryMode == SalaryMode.SPLIT) {
                    val (p1Bills, p2Bills) = distributeBillsByPart(bills, config.payday1, config.payday2)
                    val p1Total = p1Bills.sumOf { it.amountCents }
                    val p2Total = p2Bills.sumOf { it.amountCents }
                    part1 = SalaryPart(
                        amountCents = config.salary1Cents,
                        payday = config.payday1,
                        billsAssigned = p1Bills,
                        totalAssignedCents = p1Total,
                        remainingCents = config.salary1Cents - p1Total
                    )
                    part2 = SalaryPart(
                        amountCents = config.salary2Cents,
                        payday = config.payday2,
                        billsAssigned = p2Bills,
                        totalAssignedCents = p2Total,
                        remainingCents = config.salary2Cents - p2Total
                    )
                }

                HomeUiState(
                    config = config,
                    rule = rule,
                    bills = bills,
                    extras = extras,
                    upcomingBills = upcoming,
                    totalBillsCents = totalBills,
                    margin = margin,
                    extraTotalCents = extraTotal,
                    isLoading = false,
                    part1 = part1,
                    part2 = part2,
                    streakInfo = streakInfo
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun showAddExtra() {
        _uiState.update { it.copy(showAddExtraSheet = true) }
    }

    fun hideAddExtra() {
        _uiState.update { it.copy(showAddExtraSheet = false, extraName = "", extraAmountCents = 0L) }
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
                hideAddExtra()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao adicionar entrada: ${e.message}")
            }
        }
    }
}
