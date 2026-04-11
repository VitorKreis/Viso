package com.viso.domain.model

data class FinancialSummary(
    val totalCents: Long,
    val billsLimitCents: Long,
    val spendingCents: Long,
    val savingsCents: Long,
    val totalBillsCents: Long,
    val marginCents: Long,
    val extraIncomeCents: Long
)

data class SalaryPart(
    val amountCents: Long,
    val payday: Int,
    val billsAssigned: List<Bill>,
    val totalAssignedCents: Long,
    val remainingCents: Long
)

fun distributeBillsByPart(
    bills: List<Bill>,
    payday1: Int,
    payday2: Int
): Pair<List<Bill>, List<Bill>> {
    val part1Bills = bills.filter { it.dueDay >= payday1 && it.dueDay < payday2 }
    val part2Bills = bills.filter { it.dueDay >= payday2 || it.dueDay < payday1 }
    return Pair(part1Bills, part2Bills)
}
