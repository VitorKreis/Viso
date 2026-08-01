package com.viso.domain.usecase

import com.viso.data.db.entity.MonthHistoryEntity
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.data.repository.HistoryRepository
import com.viso.data.repository.PaymentHistoryRepository
import com.viso.domain.model.PaymentHistory
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

data class MonthCloseResult(
    val month: String,
    val totalBillsCents: Long,
    val paidBillsCount: Int,
    val unpaidBillsCount: Int,
    val archivedBillsCount: Int,
    val extraIncomeCents: Long,
    val monthCompleted: Boolean
)

class CloseMonthUseCase @Inject constructor(
    private val configRepo: ConfigRepository,
    private val billRepo: BillRepository,
    private val extraIncomeRepo: ExtraIncomeRepository,
    private val historyRepo: HistoryRepository,
    private val paymentHistoryRepo: PaymentHistoryRepository,
    private val scheduleNotif: ScheduleNotificationsUseCase,
    private val updateStreak: UpdateStreakUseCase,
    private val checkAchievements: CheckAchievementsUseCase
) {
    suspend operator fun invoke(month: YearMonth = YearMonth.now()): MonthCloseResult {
        val config = configRepo.getConfig()
        val monthString = month.toString()

        val allBills = billRepo.getAllBills()
        val bills = allBills.filter { billDueMonth(it, month) == month }
        val paidBills = bills.filter { it.isPaid }
        val unpaidBills = bills.filter { !it.isPaid }
        val totalBillsCents = bills.sumOf { it.amountCents }
        val extraTotal = extraIncomeRepo.getTotalForMonth(monthString)
        val rule = CalculateRuleUseCase()(config.effectiveSalaryCents, extraTotal)
        val monthCompleted = bills.isNotEmpty() && unpaidBills.isEmpty()

        paymentHistoryRepo.deleteByMonth(monthString)

        paidBills.forEach { bill ->
            paymentHistoryRepo.insert(
                PaymentHistory(
                    id = UUID.randomUUID().toString(),
                    month = monthString,
                    billId = bill.id,
                    billName = bill.name,
                    amountCents = bill.amountCents,
                    category = bill.category,
                    dueDay = bill.dueDay,
                    paidAt = System.currentTimeMillis(),
                    isRecurring = bill.isRecurring
                )
            )
        }

        historyRepo.saveMonth(
            MonthHistoryEntity(
                month = monthString,
                salaryCents = config.effectiveSalaryCents,
                totalBillsCents = totalBillsCents,
                billsLimitCents = rule.billsLimitCents,
                spendingBudgetCents = rule.spendingCents,
                savingsBudgetCents = rule.savingsCents
            )
        )

        billRepo.resetRecurringPaidStatus(monthString, month.plusMonths(1).toString())

        val archivedBills = bills.filter { !it.isRecurring && it.isPaid }
        archivedBills.forEach { bill ->
            billRepo.deleteById(bill.id)
        }

        extraIncomeRepo.deleteByMonth(monthString)

        configRepo.updateLastResetMonth(monthString)
        updateStreak(monthCompleted)
        checkAchievements()
        scheduleNotif()

        return MonthCloseResult(
            month = monthString,
            totalBillsCents = totalBillsCents,
            paidBillsCount = paidBills.size,
            unpaidBillsCount = unpaidBills.size,
            archivedBillsCount = archivedBills.size,
            extraIncomeCents = extraTotal,
            monthCompleted = monthCompleted
        )
    }
}
