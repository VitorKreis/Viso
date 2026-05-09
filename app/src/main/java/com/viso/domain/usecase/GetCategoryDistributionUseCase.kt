package com.viso.domain.usecase

import com.viso.data.repository.BillRepository
import com.viso.domain.model.CategorySpending
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import androidx.compose.ui.graphics.Color

class GetCategoryDistributionUseCase @Inject constructor(
    private val billRepository: BillRepository
) {
    operator fun invoke(): Flow<List<CategorySpending>> =
        billRepository.getAllBillsFlow().map { bills ->
            if (bills.isEmpty()) return@map emptyList()

            val totalAmount = bills.sumOf { it.amountCents }.toFloat()
            if (totalAmount == 0f) return@map emptyList()

            val categoryColors = mapOf(
                "moradia" to Color(0xFFFF6B6B),      // Vermelho coral
                "alimentacao" to Color(0xFF4ECDC4),   // Turquesa
                "transporte" to Color(0xFF45B7D1),    // Azul claro
                "saude" to Color(0xFF96CEB4),         // Verde sage
                "educacao" to Color(0xFFFFEAA7),      // Amarelo
                "utilidade" to Color(0xFFDDA0DD),     // Lilás
                "lazer" to Color(0xFFFFB347),         // Laranja
                "outro" to Color(0xFFB0C4DE)          // Azul acinzentado
            )

            bills
                .groupBy { it.category.lowercase() }
                .map { (category, categoryBills) ->
                    val amount = categoryBills.sumOf { it.amountCents }
                    val percentage = (amount / totalAmount) * 100
                    CategorySpending(
                        category = category,
                        amountCents = amount,
                        percentage = percentage,
                        color = categoryColors[category] ?: Color.Gray
                    )
                }
                .sortedByDescending { it.amountCents }
        }
}
