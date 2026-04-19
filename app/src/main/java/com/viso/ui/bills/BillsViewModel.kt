package com.viso.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.domain.model.Bill
import com.viso.domain.usecase.CalculateRuleUseCase
import com.viso.domain.usecase.FinancialRule
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

data class BillsUiState(
    val bills: List<Bill> = emptyList(),
    val billsByCategory: Map<String, List<Bill>> = emptyMap(),
    val totalBillsCents: Long = 0L,
    val rule: FinancialRule = FinancialRule(0, 0, 0, 0),
    val isLoading: Boolean = true,
    val showSheet: Boolean = false,
    val editingBill: Bill? = null,
    val billName: String = "",
    val billAmountCents: Long = 0L,
    val billDueDay: Int = 1,
    val billCategory: String = "outro",
    val billIsRecurring: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deletingBillId: String? = null
)

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val billRepo: BillRepository,
    private val configRepo: ConfigRepository,
    private val extraRepo: ExtraIncomeRepository,
    private val calculateRule: CalculateRuleUseCase,
    private val scheduleNotif: ScheduleNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillsUiState())
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()

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
                billRepo.getAllBillsFlow(),
                extraRepo.getTotalForMonthFlow(currentMonth)
            ) { config, bills, extraTotal ->
                val rule = calculateRule(config.effectiveSalaryCents, extraTotal)
                val totalBills = bills.sumOf { it.amountCents }
                val byCategory = bills.groupBy { it.category }
                BillsUiState(
                    bills = bills,
                    billsByCategory = byCategory,
                    totalBillsCents = totalBills,
                    rule = rule,
                    isLoading = false
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
                        showDeleteDialog = current.showDeleteDialog,
                        deletingBillId = current.deletingBillId
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
                billIsRecurring = false
            )
        }
    }

    fun showEditSheet(bill: Bill) {
        _uiState.update {
            it.copy(
                showSheet = true, editingBill = bill,
                billName = bill.name,
                billAmountCents = bill.amountCents,
                billDueDay = bill.dueDay,
                billCategory = bill.category,
                billIsRecurring = bill.isRecurring
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
            try {
                val bill = Bill(
                    id = state.editingBill?.id ?: UUID.randomUUID().toString(),
                    name = state.billName.trim(),
                    amountCents = state.billAmountCents,
                    dueDay = state.billDueDay,
                    category = state.billCategory,
                    isRecurring = state.billIsRecurring,
                    isPaid = state.editingBill?.isPaid ?: false,
                    paidMonth = state.editingBill?.paidMonth ?: "",
                    createdAt = state.editingBill?.createdAt ?: System.currentTimeMillis()
                )
                if (state.editingBill != null) {
                    billRepo.update(bill)
                } else {
                    billRepo.insert(bill)
                }
                scheduleNotif()
                hideSheet()
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao salvar conta: ${e.message}")
            }
        }
    }

    fun markAsPaid(billId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                billRepo.markAsPaid(billId, YearMonth.now().toString())
            } catch (e: Exception) {
                _errorEvent.emit("Erro ao marcar como paga: ${e.message}")
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
                _errorEvent.emit("Erro ao excluir conta: ${e.message}")
            }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(showDeleteDialog = false, deletingBillId = null) }
    }
}
