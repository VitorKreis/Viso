package com.viso.ui.evolution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.domain.model.MonthlySpending
import com.viso.domain.usecase.GetMonthlySpendingTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EvolutionUiState(
    val months: List<MonthlySpending> = emptyList(),
    val selectedPeriod: Int = 12,
    val isLoading: Boolean = true
)

@HiltViewModel
class EvolutionViewModel @Inject constructor(
    private val getTrend: GetMonthlySpendingTrendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EvolutionUiState())
    val uiState: StateFlow<EvolutionUiState> = _uiState.asStateFlow()

    init {
        loadData(12)
    }

    fun onPeriodChange(months: Int) {
        _uiState.update { it.copy(selectedPeriod = months) }
        loadData(months)
    }

    private fun loadData(months: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = getTrend(months)
            _uiState.value = EvolutionUiState(
                months = data,
                selectedPeriod = months,
                isLoading = false
            )
        }
    }
}
