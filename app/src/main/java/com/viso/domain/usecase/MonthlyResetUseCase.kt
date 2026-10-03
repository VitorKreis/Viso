package com.viso.domain.usecase

import com.viso.data.repository.ConfigRepository
import com.viso.domain.model.IdentityContract
import com.viso.domain.model.PaymentHistory
import com.viso.domain.repository.MonthCloseRequest
import com.viso.domain.repository.MonthCloseStore
import com.viso.domain.repository.MonthHistoryRecord
import java.time.YearMonth
import javax.inject.Inject

class MonthlyResetUseCase @Inject constructor(
    private val configRepo: ConfigRepository,
    private val monthCloseStore: MonthCloseStore,
    private val scheduleNotif: ScheduleNotificationsUseCase,
    private val generateInstallmentBills: GenerateInstallmentBillsUseCase,
    private val updateStreak: UpdateStreakUseCase,
    private val checkAchievements: CheckAchievementsUseCase
) {
    suspend operator fun invoke() {
        val config = configRepo.getConfig()
        val currentMonth = YearMonth.now()
        val currentMonthText = currentMonth.toString()

        if (config.lastResetMonth == currentMonthText) return

        if (config.lastResetMonth.isNotEmpty()) {
            val closedMonth = YearMonth.parse(config.lastResetMonth)
            val existingHistory = monthCloseStore.getHistory(config.lastResetMonth)
            if (existingHistory == null) {
                val bills = monthCloseStore.getBills()
                    .filter { billDueMonth(it, closedMonth) == closedMonth }
                val paidBills = bills.filter { it.isPaid }
                val unpaidBills = bills.filter { !it.isPaid }
                val extraTotal = monthCloseStore.getExtraIncomeTotal(config.lastResetMonth)
                val rule = CalculateRuleUseCase()(config.effectiveSalaryCents, extraTotal)
                val payments = paidBills.map { bill ->
                    PaymentHistory(
                        id = "payment:${config.lastResetMonth}:${IdentityContract.requireId(bill.id, "billId")}",
                        month = config.lastResetMonth,
                        billId = bill.id,
                        billName = bill.name,
                        amountCents = bill.amountCents,
                        category = bill.category,
                        dueDay = bill.dueDay,
                        paidAt = System.currentTimeMillis(),
                        isRecurring = bill.isRecurring
                    )
                }
                val didClose = monthCloseStore.close(
                    MonthCloseRequest(
                        month = config.lastResetMonth,
                        nextMonth = currentMonthText,
                        history = MonthHistoryRecord(
                            month = config.lastResetMonth,
                            salaryCents = config.effectiveSalaryCents,
                            totalBillsCents = bills.sumOf { it.amountCents },
                            billsLimitCents = rule.billsLimitCents,
                            spendingBudgetCents = rule.spendingCents,
                            savingsBudgetCents = rule.savingsCents
                        ),
                        payments = payments,
                        archivedBillIds = bills.filter { !it.isRecurring && it.isPaid }.map { it.id }
                    )
                )
                if (didClose) {
                    updateStreak(bills.isNotEmpty() && unpaidBills.isEmpty())
                    checkAchievements()
                }
            }
        }

        configRepo.updateLastResetMonth(currentMonthText)
        generateInstallmentBills.generateBillsForMonth(currentMonthText)
        scheduleNotif()
    }
}
