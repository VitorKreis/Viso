package com.viso.ui.reports

import com.viso.data.db.dao.MonthHistoryDao
import javax.inject.Inject

data class MonthlyReport(
    val month: String,
    val billsCents: Long,
    val spendingCents: Long,
    val savingsCents: Long
)

class ReportsRepository @Inject constructor(
    private val monthHistoryDao: MonthHistoryDao
) {
    suspend fun getLast12Months(): List<MonthlyReport> {
        val all = monthHistoryDao.getAll()
        // month format is YYYY-MM, sort ascending
        val sorted = all.sortedBy { it.month }
        val last12 = if (sorted.size <= 12) sorted else sorted.takeLast(12)

        return last12.map { m ->
            MonthlyReport(
                month = m.month,
                billsCents = m.totalBillsCents,
                spendingCents = m.spendingBudgetCents,
                savingsCents = m.savingsBudgetCents
            )
        }
    }
}
