package com.viso.domain.usecase

import com.viso.data.db.entity.MonthHistoryEntity
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.data.repository.HistoryRepository
import java.time.YearMonth
import javax.inject.Inject

class MonthlyResetUseCase @Inject constructor(
    private val configRepo: ConfigRepository,
    private val billRepo: BillRepository,
    private val extraIncomeRepo: ExtraIncomeRepository,
    private val historyRepo: HistoryRepository,
    private val scheduleNotif: ScheduleNotificationsUseCase
) {
    suspend operator fun invoke() {
        val config = configRepo.getConfig()
        val currentMonth = YearMonth.now().toString()

        if (config.lastResetMonth == currentMonth) return

        if (config.lastResetMonth.isNotEmpty()) {
            val bills = billRepo.getAllBills()
            val totalBillsCents = bills.sumOf { it.amountCents }
            val extraTotal = extraIncomeRepo.getTotalForMonth(config.lastResetMonth)
            val rule = CalculateRuleUseCase()(config.salaryCents, extraTotal)
            historyRepo.saveMonth(
                MonthHistoryEntity(
                    month = config.lastResetMonth,
                    salaryCents = config.salaryCents,
                    totalBillsCents = totalBillsCents,
                    billsLimitCents = rule.billsLimitCents,
                    spendingBudgetCents = rule.spendingCents,
                    savingsBudgetCents = rule.savingsCents
                )
            )

            billRepo.resetAllPaidStatus(currentMonth)
            extraIncomeRepo.deleteByMonth(config.lastResetMonth)
        }

        configRepo.updateLastResetMonth(currentMonth)
        scheduleNotif()
    }
}
