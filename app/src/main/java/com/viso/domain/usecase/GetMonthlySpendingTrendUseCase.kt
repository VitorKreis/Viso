package com.viso.domain.usecase

import com.viso.data.db.dao.BillDao
import com.viso.data.db.dao.MonthSpendingTuple
import com.viso.domain.model.MonthlySpending
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

class GetMonthlySpendingTrendUseCase @Inject constructor(
    private val billDao: BillDao
) {
    suspend operator fun invoke(monthsBack: Int = 12): List<MonthlySpending> {
        val today = YearMonth.now()
        val startMonth = today.minusMonths(monthsBack.toLong() - 1)

        // Get spending from database
        val dbSpending = billDao.getMonthlySpending()
        val dbMap = dbSpending.associateBy { it.paidMonth }

        // Build complete list, filling missing months with 0
        return (0 until monthsBack).map { offset ->
            val yearMonth = startMonth.plusMonths(offset.toLong())
            val key = yearMonth.toString()
            val data = dbMap[key]
            MonthlySpending(
                yearMonth = key,
                totalCents = data?.totalCents ?: 0L,
                label = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            )
        }
    }
}
