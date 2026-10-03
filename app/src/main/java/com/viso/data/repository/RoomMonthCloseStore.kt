package com.viso.data.repository

import androidx.room.withTransaction
import com.viso.data.db.VisoDB
import com.viso.data.db.toDomainBill
import com.viso.data.db.toDomainPaymentHistory
import com.viso.data.db.toEntity
import com.viso.data.db.toRecord
import com.viso.domain.model.Bill
import com.viso.domain.model.PaymentHistory
import com.viso.domain.repository.MonthCloseRequest
import com.viso.domain.repository.MonthCloseStore
import com.viso.domain.repository.MonthHistoryRecord
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMonthCloseStore @Inject constructor(
    private val db: VisoDB
) : MonthCloseStore {

    override suspend fun getBills(): List<Bill> =
        db.billDao().getAllBillsList().map { it.toDomainBill() }

    override suspend fun getExtraIncomeTotal(month: String): Long =
        db.extraIncomeDao().getTotalForMonth(month)

    override suspend fun getHistory(month: String): MonthHistoryRecord? =
        db.monthHistoryDao().getByMonth(month)?.toRecord()

    override suspend fun getPayments(month: String): List<PaymentHistory> =
        db.paymentHistoryDao().getByMonth(month).map { it.toDomainPaymentHistory() }

    override suspend fun close(request: MonthCloseRequest): Boolean = db.withTransaction {
        // month_history is the durable idempotency key. The check is inside the
        // same SQLite transaction as every write below, so concurrent close calls
        // cannot both mutate the month.
        if (db.monthHistoryDao().getByMonth(request.month) != null) return@withTransaction false

        db.paymentHistoryDao().deleteByMonth(request.month)
        request.payments.forEach { payment ->
            db.paymentHistoryDao().insert(payment.toEntity())
        }
        db.monthHistoryDao().insert(request.history.toEntity())
        db.billDao().resetRecurringPaidStatus(request.month, request.nextMonth)
        request.archivedBillIds.forEach { billId -> db.billDao().deleteById(billId) }
        db.extraIncomeDao().deleteByMonth(request.month)
        true
    }
}
