package com.viso.domain.usecase

import com.viso.domain.model.Bill
import java.time.LocalDate
import java.time.YearMonth

fun billsMargin(billsLimitCents: Long, totalBillsCents: Long): Long =
    billsLimitCents - totalBillsCents

fun emergencyFundTarget(totalMonthlyBillsCents: Long): Long =
    totalMonthlyBillsCents * 3

fun monthsToGoal(targetCents: Long, currentCents: Long, monthlyCents: Long): Int? {
    if (monthlyCents <= 0) return null
    val remaining = targetCents - currentCents
    if (remaining <= 0) return 0
    return ((remaining + monthlyCents - 1) / monthlyCents).toInt()
}

fun clampDayToMonth(day: Int, year: Int, month: Int): Int {
    val maxDay = YearMonth.of(year, month).lengthOfMonth()
    return day.coerceIn(1, maxDay)
}

enum class BillStatus { PAID, TODAY, UPCOMING, OVERDUE, FUTURE }

fun getBillStatus(bill: Bill, today: LocalDate): BillStatus {
    if (bill.isPaid) return BillStatus.PAID
    val dueDate = today.withDayOfMonth(
        clampDayToMonth(bill.dueDay, today.year, today.monthValue)
    )
    return when {
        dueDate == today -> BillStatus.TODAY
        dueDate < today -> BillStatus.OVERDUE
        dueDate <= today.plusDays(7) -> BillStatus.UPCOMING
        else -> BillStatus.FUTURE
    }
}
