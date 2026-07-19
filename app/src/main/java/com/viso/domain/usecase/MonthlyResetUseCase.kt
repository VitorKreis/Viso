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

class MonthlyResetUseCase @Inject constructor(
    private val configRepo: ConfigRepository,
    private val billRepo: BillRepository,
    private val extraIncomeRepo: ExtraIncomeRepository,
    private val historyRepo: HistoryRepository,
    private val paymentHistoryRepo: PaymentHistoryRepository,
    private val scheduleNotif: ScheduleNotificationsUseCase,
    private val generateInstallmentBills: GenerateInstallmentBillsUseCase,
    private val updateStreak: UpdateStreakUseCase,
    private val checkAchievements: CheckAchievementsUseCase
) {
    suspend operator fun invoke() {
        val config = configRepo.getConfig()
        val currentMonth = YearMonth.now().toString()

        if (config.lastResetMonth == currentMonth) return

        if (config.lastResetMonth.isNotEmpty()) {
            val bills = billRepo.getAllBills()
            val totalBillsCents = bills.sumOf { it.amountCents }
            val extraTotal = extraIncomeRepo.getTotalForMonth(config.lastResetMonth)
            val rule = CalculateRuleUseCase()(config.effectiveSalaryCents, extraTotal)
            val monthCompleted = bills.isNotEmpty() && bills.all { it.isPaid }

            paymentHistoryRepo.deleteByMonth(config.lastResetMonth)
            
            // 1. Save detailed payment history for paid bills
            bills.filter { it.isPaid }.forEach { bill ->
                paymentHistoryRepo.insert(
                    PaymentHistory(
                        id = UUID.randomUUID().toString(),
                        month = config.lastResetMonth,
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
            
            // 2. Save month summary
            historyRepo.saveMonth(
                MonthHistoryEntity(
                    month = config.lastResetMonth,
                    salaryCents = config.effectiveSalaryCents,
                    totalBillsCents = totalBillsCents,
                    billsLimitCents = rule.billsLimitCents,
                    spendingBudgetCents = rule.spendingCents,
                    savingsBudgetCents = rule.savingsCents
                )
            )

            // 3. Reset paid status for recurring bills only
            billRepo.resetRecurringPaidStatus()
            
            // 4. Delete avulsas (non-recurring) that were paid
            bills.filter { !it.isRecurring && it.isPaid }.forEach { bill ->
                billRepo.deleteById(bill.id)
            }
            
            extraIncomeRepo.deleteByMonth(config.lastResetMonth)
            updateStreak(monthCompleted)
            checkAchievements()
        }

        configRepo.updateLastResetMonth(currentMonth)

        // Generate installment bills for the new month
        generateInstallmentBills.generateBillsForMonth(currentMonth)

        scheduleNotif()
    }
}
