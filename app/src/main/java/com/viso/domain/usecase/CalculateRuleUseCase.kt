package com.viso.domain.usecase

import javax.inject.Inject

data class FinancialRule(
    val totalCents: Long,
    val billsLimitCents: Long,
    val spendingCents: Long,
    val savingsCents: Long
)

class CalculateRuleUseCase @Inject constructor() {
    operator fun invoke(salaryCents: Long, extraIncomeCents: Long): FinancialRule {
        val base = salaryCents + extraIncomeCents
        return FinancialRule(
            totalCents = base,
            billsLimitCents = (base * 70) / 100,
            spendingCents = (base * 20) / 100,
            savingsCents = (base * 10) / 100
        )
    }
}
