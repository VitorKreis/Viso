package com.viso.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.data.repository.GoalRepository
import com.viso.data.repository.InstallmentBillRepository
import com.viso.domain.model.Bill
import com.viso.domain.model.FinancialRadar
import com.viso.domain.model.InstallmentBill
import com.viso.domain.usecase.CalculateFinancialRadarUseCase
import com.viso.domain.usecase.CalculateRuleUseCase
import com.viso.domain.usecase.FinancialRule
import com.viso.domain.usecase.GenerateInstallmentBillsUseCase
import com.viso.domain.usecase.ScheduleNotificationsUseCase
import com.viso.domain.usecase.billDueMonth
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

enum class BillFilter {
    ALL, PENDING, PAID
}

data class BillsUiState(
    val bills: List<Bill> = emptyList(),
    val billsByCategory: Map<String, List<Bill>> = emptyMap(),
    val totalBillsCents: Long = 0L,
    val pendingBillsCents: Long = 0L,
    val rule: FinancialRule = FinancialRule(0, 0, 0, 0),
    val isLoading: Boolean = true,
    val showSheet: Boolean = false,
    val editingBill: Bill? = null,
    val billName: String = "",
    val billAmountCents: Long = 0L,
    val billDueDay: Int = 1,
    val billCategory: String = "outro",
    val billIsRecurring: Boolean = false,
    val billMonth: String = "",
    val showMonthPicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deletingBillId: String? = null,
    // Installment fields
    val isInstallment: Boolean = false,
    val totalInstallments: Int = 2,
    val installmentStartMonth: String = YearMonth.now().toString(),
    // Filter
    val filter: BillFilter = BillFilter.ALL,
    val paidBillsCount: Int = 0,
    val pendingBillsCount: Int = 0,
    // Separated sections
    val recurringBills: List<Bill> = emptyList(),
    val nonRecurringBills: List<Bill> = emptyList(),
    val isMonthPrepared: Boolean = true,
    val currentMonth: String = YearMonth.now().toString(),
    val selectedMonth: String = YearMonth.now().toString(),
    val radar: FinancialRadar? = null
)

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val billRepo: BillRepository,
    private val configRepo: ConfigRepository,
    private val extraRepo: ExtraIncomeRepository,
    private val installmentBillRepo: InstallmentBillRepository,
    private val goalRepo: GoalRepository,
    private val calculateRule: CalculateRuleUseCase,
    private val calculateRadar: CalculateFinancialRadarUseCase,
    private val scheduleNotif: ScheduleNotificationsUseCase,
    private val generateInstallmentBills: GenerateInstallmentBillsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillsUiState())
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()
    private val filterFlow = MutableStateFlow(BillFilter.ALL)
    private val selectedMonthFlow = MutableStateFlow(YearMonth.now().toString())

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val currentMonth = YearMonth.now().toString()
        viewModelScope.launch(Dispatchers.IO) {
            val listControlsFlow = combine(filterFlow, selectedMonthFlow) { filter, selectedMonth ->
                filter to selectedMonth
            }
            combine(
                configRepo.configFlow,
                billRepo.getAllBillsFlow(),
                goalRepo.getAllGoalsFlow(),
                extraRepo.getTotalForMonthFlow(currentMonth),
                listControlsFlow
            ) { config, allBills, goals, extraTotal, listControls ->
                val (currentFilter, selectedMonth) = listControls
                val selectedYearMonth = parseMonth(selectedMonth)
                val monthBills = allBills.filter { billDueMonth(it, selectedYearMonth) == selectedYearMonth }
                val rule = calculateRule(config.effectiveSalaryCents, extraTotal)
                val radar = calculateRadar(rule, monthBills, goals, selectedYearMonth)

                // Count paid and pending bills
                val paidCount = monthBills.count { it.isPaid }
                val pendingCount = monthBills.size - paidCount
                val pendingBillsTotal = monthBills.filter { !it.isPaid }.sumOf { it.amountCents }

                // Apply filter based on current state
                val filteredBills = when (currentFilter) {
                    BillFilter.ALL -> monthBills
                    BillFilter.PENDING -> monthBills.filter { !it.isPaid }
                    BillFilter.PAID -> monthBills.filter { it.isPaid }
                }

                val totalBills = monthBills.sumOf { it.amountCents }
                val byCategory = filteredBills.groupBy { it.category }

                // Separate recurring and non-recurring bills
                val recurringBills = monthBills.filter { it.isRecurring || it.isInstallment }
                val nonRecurringBills = monthBills.filter { !it.isRecurring && !it.isInstallment }

                BillsUiState(
                    bills = filteredBills,
                    billsByCategory = byCategory,
                    totalBillsCents = totalBills,
                    pendingBillsCents = pendingBillsTotal,
                    rule = rule,
                    isLoading = false,
                    filter = currentFilter,
                    paidBillsCount = paidCount,
                    pendingBillsCount = pendingCount,
                    recurringBills = recurringBills,
                    nonRecurringBills = nonRecurringBills,
                    isMonthPrepared = config.monthSetupPreparedMonth == currentMonth,
                    currentMonth = currentMonth,
                    selectedMonth = selectedMonth,
                    radar = radar
                )
            }.collect { state ->
                _uiState.update { current ->
                    state.copy(
                        showSheet = current.showSheet,
                        editingBill = current.editingBill,
                        billName = current.billName,
                        billAmountCents = current.billAmountCents,
                        billDueDay = current.billDueDay,
                        billCategory = current.billCategory,
                        billIsRecurring = current.billIsRecurring,
                        billMonth = current.billMonth,
                        showMonthPicker = current.showMonthPicker,
                        showDeleteDialog = current.showDeleteDialog,
                        deletingBillId = current.deletingBillId,
                        isInstallment = current.isInstallment,
                        totalInstallments = current.totalInstallments,
                        installmentStartMonth = current.installmentStartMonth
                    )
                }
            }
        }
    }

    fun showAddSheet() {
        _uiState.update {
            it.copy(
                showSheet = true, editingBill = null,
                billName = "", billAmountCents = 0L,
                billDueDay = 1, billCategory = "outro",
                billIsRecurring = false,
                billMonth = YearMonth.now().toString(),
                showMonthPicker = false,
                // Reset installment fields
                isInstallment = false,
                totalInstallments = 2,
                installmentStartMonth = YearMonth.now().toString()
            )
        }
    }

    fun showEditSheet(bill: Bill) {
        // If it's an installment bill, show info message
        if (bill.isInstallment) {
            viewModelScope.launch {
                _errorEvent.emit("Contas parceladas não podem ser editadas individualmente. Cancele o parcelamento e crie novamente.")
            }
            return
        }

        _uiState.update {
            it.copy(
                showSheet = true, editingBill = bill,
                billName = bill.name,
                billAmountCents = bill.amountCents,
                billDueDay = bill.dueDay,
                billCategory = bill.category,
                billIsRecurring = bill.isRecurring,
                billMonth = bill.dueMonth.ifBlank { bill.paidMonth.ifBlank { YearMonth.now().toString() } },
                showMonthPicker = false,
                // Reset installment fields for regular edit
                isInstallment = false
            )
        }
    }

    fun hideSheet() {
        _uiState.update { it.copy(showSheet = false, editingBill = null) }
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

    fun onBillIsRecurringChange(isRecurring: Boolean) {
        _uiState.update { it.copy(billIsRecurring = isRecurring) }
    }

    fun onBillMonthChange(month: String) {
        _uiState.update { it.copy(billMonth = month) }
    }

    fun showMonthPicker() {
        _uiState.update { it.copy(showMonthPicker = true) }
    }

    fun hideMonthPicker() {
        _uiState.update { it.copy(showMonthPicker = false) }
    }

    // Installment field handlers
    fun onIsInstallmentChange(isInstallment: Boolean) {
        _uiState.update { it.copy(isInstallment = isInstallment) }
    }

    fun onTotalInstallmentsChange(count: Int) {
        _uiState.update { it.copy(totalInstallments = count.coerceIn(2, 48)) }
    }

    fun onInstallmentStartMonthChange(month: String) {
        _uiState.update { it.copy(installmentStartMonth = month) }
    }

    fun saveBill() {
        val state = _uiState.value
        if (state.billName.isBlank()) {
            viewModelScope.launch { _errorEvent.emit("Digite o nome da conta") }
            return
        }
        if (state.billAmountCents <= 0) {
            viewModelScope.launch { _errorEvent.emit("Digite um valor válido") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val isEditing = state.editingBill != null
            try {
                if (state.isInstallment && state.editingBill == null) {
                    // Create installment bill
                    saveInstallmentBill(state)
                } else {
                    // Create or update regular bill
                    saveRegularBill(state)
                }
                hideSheet()
                selectedMonthFlow.value = state.billMonth
                _errorEvent.emit(if (isEditing) "Conta atualizada." else "Conta criada.")
            } catch (e: Exception) {
                _errorEvent.emit(formatError(if (isEditing) "Erro ao atualizar conta" else "Erro ao criar conta", e))
                return@launch
            }

            if (!isEditing) {
                try {
                    configRepo.markMonthPrepared(YearMonth.now().toString())
                } catch (e: Exception) {
                    _errorEvent.emit(formatError("Conta criada, mas não consegui pausar o lembrete de abertura do mês", e))
                }
            }

            try {
                scheduleNotif()
            } catch (e: Exception) {
                _errorEvent.emit(formatError("Conta salva, mas houve erro ao reagendar notificações", e))
            }
        }
    }

    private suspend fun saveRegularBill(state: BillsUiState) {
        val bill = Bill(
            id = state.editingBill?.id ?: UUID.randomUUID().toString(),
            name = state.billName.trim(),
            amountCents = state.billAmountCents,
            dueDay = state.billDueDay,
            category = state.billCategory,
            isRecurring = state.billIsRecurring,
            paidMonth = if (state.editingBill?.isPaid == true) state.billMonth else state.editingBill?.paidMonth.orEmpty(),
            dueMonth = state.billMonth,
            isPaid = state.editingBill?.isPaid ?: false,
            createdAt = state.editingBill?.createdAt ?: System.currentTimeMillis(),
            isInstallment = false
        )
        if (state.editingBill != null) {
            billRepo.update(bill)
        } else {
            billRepo.insert(bill)
        }
    }

    private suspend fun saveInstallmentBill(state: BillsUiState) {
        val installmentAmount = generateInstallmentBills.calculateInstallmentAmount(
            state.billAmountCents,
            state.totalInstallments,
            1
        )

        val installmentBill = InstallmentBill(
            id = UUID.randomUUID().toString(),
            name = state.billName.trim(),
            totalAmountCents = state.billAmountCents,
            installmentAmountCents = installmentAmount,
            totalInstallments = state.totalInstallments,
            startMonth = state.installmentStartMonth,
            category = state.billCategory,
            dueDay = state.billDueDay,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )

        installmentBillRepo.insert(installmentBill)
        generateInstallmentBills.generateInitialBill(installmentBill)
    }

    fun markAsPaid(billId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bill = billRepo.getBillById(billId)
                val paidMonth = bill?.let { billDueMonth(it).toString() } ?: YearMonth.now().toString()
                billRepo.markAsPaid(billId, paidMonth)
            } catch (e: Exception) {
                _errorEvent.emit(formatError("Erro ao marcar conta como paga", e))
            }
        }
    }

    fun requestDelete(billId: String) {
        _uiState.update { it.copy(showDeleteDialog = true, deletingBillId = billId) }
    }

    fun confirmDelete() {
        val id = _uiState.value.deletingBillId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                billRepo.deleteById(id)
                scheduleNotif()
                _uiState.update { it.copy(showDeleteDialog = false, deletingBillId = null) }
            } catch (e: Exception) {
                _errorEvent.emit(formatError("Erro ao excluir conta", e))
            }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(showDeleteDialog = false, deletingBillId = null) }
    }

    fun setFilter(filter: BillFilter) {
        filterFlow.value = filter
    }

    fun setSelectedMonth(month: String) {
        selectedMonthFlow.value = month
    }

    fun markMonthPrepared() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                configRepo.markMonthPrepared(YearMonth.now().toString())
                scheduleNotif()
                _errorEvent.emit("Mes revisado. Lembretes de abertura pausados.")
            } catch (e: Exception) {
                _errorEvent.emit(formatError("Erro ao revisar mes", e))
            }
        }
    }

    private fun formatError(prefix: String, error: Throwable): String {
        val type = error::class.simpleName ?: "Erro desconhecido"
        val detail = error.message?.takeIf { it.isNotBlank() } ?: "sem detalhe técnico"
        return "$prefix ($type): $detail"
    }

    private fun parseMonth(month: String): YearMonth =
        runCatching { YearMonth.parse(month) }.getOrDefault(YearMonth.now())
}
