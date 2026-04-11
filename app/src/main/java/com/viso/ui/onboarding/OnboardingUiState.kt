package com.viso.ui.onboarding

import com.viso.domain.model.Bill
import com.viso.domain.model.SalaryMode
import com.viso.domain.usecase.FinancialRule

data class OnboardingUiState(
    val step: Int = 0,
    val salaryCents: Long = 0L,
    val payday: Int = 5,
    val salaryMode: SalaryMode = SalaryMode.SINGLE,
    val salary1Cents: Long = 0L,
    val payday1: Int = 5,
    val salary2Cents: Long = 0L,
    val payday2: Int = 20,
    val paydayError: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val showBillForm: Boolean = false,
    val billName: String = "",
    val billAmountCents: Long = 0L,
    val billDueDay: Int = 1,
    val billCategory: String = "outro",
    val rule: FinancialRule = FinancialRule(0, 0, 0, 0),
    val totalBillsCents: Long = 0L
) {
    val effectiveSalaryCents: Long
        get() = if (salaryMode == SalaryMode.SPLIT) salary1Cents + salary2Cents else salaryCents
}
