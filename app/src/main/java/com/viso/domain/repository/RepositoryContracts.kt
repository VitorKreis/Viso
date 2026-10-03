package com.viso.domain.repository

import com.viso.domain.model.Bill
import com.viso.domain.model.Goal
import com.viso.domain.model.PaymentHistory
import com.viso.domain.model.Config
import kotlinx.coroutines.flow.Flow

interface BillRepositoryContract {
    fun getAllBillsFlow(): Flow<List<Bill>>
    suspend fun getAllBills(): List<Bill>
    suspend fun insert(bill: Bill)
}

interface GoalRepositoryContract {
    suspend fun getAllGoals(): List<Goal>
    suspend fun insert(goal: Goal)
}

interface ConfigRepositoryContract {
    suspend fun getConfig(): Config
    suspend fun updateLastResetMonth(month: String)
}

/** Remote persistence contract; the domain does not know the provider. */
interface RemoteDataSource {
    suspend fun syncBill(bill: Bill): Result<Unit>
    suspend fun deleteBill(billId: String): Result<Unit>
    suspend fun syncBillPaidStatus(billId: String, isPaid: Boolean, paidMonth: String): Result<Unit>
    suspend fun getAllBillsOnce(): Result<List<Bill>>
    suspend fun getAllGoalsOnce(): Result<List<Goal>>
    suspend fun syncGoal(goal: Goal): Result<Unit>
    suspend fun deleteGoal(goalId: String): Result<Unit>
}

data class MonthHistoryRecord(
    val month: String,
    val salaryCents: Long,
    val totalBillsCents: Long,
    val billsLimitCents: Long,
    val spendingBudgetCents: Long,
    val savingsBudgetCents: Long
)

data class MonthCloseRequest(
    val month: String,
    val nextMonth: String,
    val history: MonthHistoryRecord,
    val payments: List<PaymentHistory>,
    val archivedBillIds: List<String>
)

interface MonthCloseStore {
    suspend fun getBills(): List<Bill>
    suspend fun getExtraIncomeTotal(month: String): Long
    suspend fun getHistory(month: String): MonthHistoryRecord?
    suspend fun getPayments(month: String): List<PaymentHistory>

    /**
     * Returns true when this request performed the first close. A false result
     * means another execution already closed the month and no writes were made.
     */
    suspend fun close(request: MonthCloseRequest): Boolean
}

interface MonthCloseSideEffects {
    suspend fun onAlreadyClosed()
    suspend fun onSuccessfulClose(monthCompleted: Boolean)
}
