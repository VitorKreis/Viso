package com.viso.domain.usecase

import com.viso.domain.model.IdentityContract
import com.viso.domain.model.PaymentHistory
import com.viso.domain.repository.ConfigRepositoryContract
import com.viso.domain.repository.MonthCloseRequest
import com.viso.domain.repository.MonthCloseStore
import com.viso.domain.repository.MonthHistoryRecord
import com.viso.domain.repository.MonthCloseSideEffects
import java.time.YearMonth
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
    private val configRepo: ConfigRepositoryContract,
    private val monthCloseStore: MonthCloseStore,
    private val sideEffects: MonthCloseSideEffects
) {
    suspend operator fun invoke(month: YearMonth = YearMonth.now()): MonthCloseResult {
        val config = configRepo.getConfig()
        val monthString = month.toString()

        // month_history is the durable idempotency marker. A retry can repair
        // DataStore without replaying any financial operation.
        val alreadyClosed = monthCloseStore.getHistory(monthString)
        if (alreadyClosed != null) {
            if (config.lastResetMonth != monthString) {
                configRepo.updateLastResetMonth(monthString)
            }
            sideEffects.onAlreadyClosed()
            val payments = monthCloseStore.getPayments(monthString)
            return MonthCloseResult(
                month = monthString,
                totalBillsCents = alreadyClosed.totalBillsCents,
                paidBillsCount = payments.size,
                unpaidBillsCount = 0,
                archivedBillsCount = 0,
                extraIncomeCents = 0L,
                monthCompleted = payments.isNotEmpty()
            )
        }

        val allBills = monthCloseStore.getBills()
        val bills = allBills.filter { billDueMonth(it, month) == month }
        val paidBills = bills.filter { it.isPaid }
        val unpaidBills = bills.filter { !it.isPaid }
        val totalBillsCents = bills.sumOf { it.amountCents }
        val extraTotal = monthCloseStore.getExtraIncomeTotal(monthString)
        val rule = CalculateRuleUseCase()(config.effectiveSalaryCents, extraTotal)
        val monthCompleted = bills.isNotEmpty() && unpaidBills.isEmpty()
        val archivedBills = bills.filter { !it.isRecurring && it.isPaid }

        val payments = paidBills.map { bill ->
            PaymentHistory(
                // Stable within a month: the same bill cannot create a second
                // payment history record on a repeated close.
                id = "payment:$monthString:${IdentityContract.requireId(bill.id, "billId")}",
                month = monthString,
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
                month = monthString,
                nextMonth = month.plusMonths(1).toString(),
                history = MonthHistoryRecord(
                    month = monthString,
                    salaryCents = config.effectiveSalaryCents,
                    totalBillsCents = totalBillsCents,
                    billsLimitCents = rule.billsLimitCents,
                    spendingBudgetCents = rule.spendingCents,
                    savingsBudgetCents = rule.savingsCents
                ),
                payments = payments,
                archivedBillIds = archivedBills.map { it.id }
            )
        )

        if (!didClose) {
            // Another invocation won the transaction. The history path is safe
            // and performs no financial writes.
            return invoke(month)
        }

        // Room is committed before DataStore and notification side effects. If
        // either fails, a retry repairs it without replaying the Room close.
        configRepo.updateLastResetMonth(monthString)
        sideEffects.onSuccessfulClose(monthCompleted)

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
