package com.viso.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReportFilter { ALL, BILLS, SPENDING, SAVINGS }

data class ReportsUiState(
    val months: List<MonthlyReport> = emptyList(),
    val filter: ReportFilter = ReportFilter.ALL,
    val indicator: String = ""
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ReportsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val months = repository.getLast12Months()
            val indicator = computeIndicator(months)
            _uiState.value = ReportsUiState(months = months, filter = ReportFilter.ALL, indicator = indicator)
        }
    }

    fun setFilter(filter: ReportFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    private fun computeIndicator(months: List<MonthlyReport>): String {
        if (months.size < 2) return "Sem dados suficientes"
        val last = months.last()
        val prev = months[months.lastIndex - 1]

        val savedDelta = last.savingsCents - prev.savingsCents
        val spentDelta = last.spendingCents - prev.spendingCents

        return when {
            savedDelta > 0 && savedDelta > kotlin.math.abs(spentDelta) -> "Economizou mais"
            spentDelta > 0 && kotlin.math.abs(spentDelta) > kotlin.math.abs(savedDelta) -> "Gastou além do planejado"
            else -> "Tendência estável"
        }
    }
}
