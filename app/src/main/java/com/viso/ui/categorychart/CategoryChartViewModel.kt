package com.viso.ui.categorychart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.domain.usecase.GetCategoryDistributionUseCase
import com.viso.ui.components.PieSegment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryChartUiState(
    val segments: List<PieSegment> = emptyList(),
    val categorySpendings: List<com.viso.domain.model.CategorySpending> = emptyList(),
    val totalAmount: Long = 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class CategoryChartViewModel @Inject constructor(
    private val getCategoryDistribution: GetCategoryDistributionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryChartUiState())
    val uiState: StateFlow<CategoryChartUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getCategoryDistribution().collect { spendings ->
                val total = spendings.sumOf { it.amountCents }
                val segments = spendings.map { spending ->
                    PieSegment(
                        value = spending.amountCents.toFloat(),
                        color = spending.color,
                        label = spending.category
                    )
                }

                _uiState.update {
                    it.copy(
                        segments = segments,
                        categorySpendings = spendings,
                        totalAmount = total,
                        isLoading = false
                    )
                }
            }
        }
    }
}
