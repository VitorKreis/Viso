package com.viso.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.domain.model.Bill
import com.viso.domain.model.Config
import com.viso.domain.model.ExtraIncome
import com.viso.domain.model.SalaryMode
import com.viso.ui.components.CalendarEvent
import com.viso.ui.theme.AccentAmber
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentRed
import com.viso.domain.usecase.BillStatus
import com.viso.domain.usecase.clampDayToMonth
import com.viso.domain.usecase.getBillStatus
import com.viso.ui.utils.formatCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class AgendaEvent(
    val day: Int,
    val name: String,
    val amountCents: Long,
    val isIncome: Boolean,
    val isPaid: Boolean
)

data class AgendaUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val events: List<CalendarEvent> = emptyList(),
    val agendaEvents: List<AgendaEvent> = emptyList(),
    val filteredEvents: List<AgendaEvent> = emptyList(),
    val selectedDay: Int? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val billRepo: BillRepository,
    private val extraRepo: ExtraIncomeRepository,
    private val configRepo: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val ym = _uiState.value.yearMonth
            combine(
                billRepo.getAllBillsFlow(),
                extraRepo.getByMonthFlow(ym.toString()),
                configRepo.configFlow
            ) { bills, extras, config ->
                buildState(bills, extras, config, ym)
            }.collect { state ->
                _uiState.update { current ->
                    state.copy(selectedDay = current.selectedDay)
                }
            }
        }
    }

    private fun buildState(bills: List<Bill>, extras: List<ExtraIncome>, config: Config, ym: YearMonth): AgendaUiState {
        val today = LocalDate.now()
        val calendarEvents = mutableListOf<CalendarEvent>()
        val agendaEvents = mutableListOf<AgendaEvent>()

        bills.forEach { bill ->
            val day = clampDayToMonth(bill.dueDay, ym.year, ym.monthValue)
            val status = getBillStatus(bill, today)
            val color = when (status) {
                BillStatus.PAID -> AccentBlue
                BillStatus.OVERDUE, BillStatus.TODAY -> AccentRed
                BillStatus.UPCOMING -> AccentAmber
                BillStatus.FUTURE -> AccentAmber
            }
            calendarEvents.add(CalendarEvent(day, color, bill.name))
            agendaEvents.add(AgendaEvent(day, bill.name, bill.amountCents, isIncome = false, isPaid = bill.isPaid))
        }

        extras.forEach { extra ->
            calendarEvents.add(CalendarEvent(1, AccentGreen, extra.name))
            agendaEvents.add(AgendaEvent(1, extra.name, extra.amountCents, isIncome = true, isPaid = false))
        }

        if (config.salaryMode == SalaryMode.SPLIT) {
            val day1 = clampDayToMonth(config.payday1, ym.year, ym.monthValue)
            val day2 = clampDayToMonth(config.payday2, ym.year, ym.monthValue)
            calendarEvents.add(CalendarEvent(day1, AccentGreen, "Parcela 1"))
            calendarEvents.add(CalendarEvent(day2, AccentGreen, "Parcela 2"))
            agendaEvents.add(AgendaEvent(day1, "Parcela 1 · ${formatCurrency(config.salary1Cents)}", config.salary1Cents, isIncome = true, isPaid = false))
            agendaEvents.add(AgendaEvent(day2, "Parcela 2 · ${formatCurrency(config.salary2Cents)}", config.salary2Cents, isIncome = true, isPaid = false))
        }

        agendaEvents.sortBy { it.day }

        return AgendaUiState(
            yearMonth = ym,
            events = calendarEvents,
            agendaEvents = agendaEvents,
            filteredEvents = agendaEvents,
            isLoading = false
        )
    }

    fun onDayClick(day: Int) {
        _uiState.update { state ->
            val newSelected = if (state.selectedDay == day) null else day
            val filtered = if (newSelected != null) {
                state.agendaEvents.filter { it.day == newSelected }
            } else {
                state.agendaEvents
            }
            state.copy(selectedDay = newSelected, filteredEvents = filtered)
        }
    }

    fun previousMonth() {
        _uiState.update { it.copy(yearMonth = it.yearMonth.minusMonths(1), selectedDay = null) }
        loadData()
    }

    fun nextMonth() {
        _uiState.update { it.copy(yearMonth = it.yearMonth.plusMonths(1), selectedDay = null) }
        loadData()
    }
}
