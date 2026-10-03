package com.viso.data.repository

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viso.data.db.VisoDB
import com.viso.data.db.entity.BillEntity
import com.viso.domain.model.PaymentHistory
import com.viso.domain.repository.MonthCloseRequest
import com.viso.domain.repository.MonthHistoryRecord
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMonthCloseStoreTest {
    private val month = YearMonth.of(2026, 4)

    @Test
    fun closeIsAtomicAndSecondExecutionIsIdempotent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, VisoDB::class.java).build()
        val store = RoomMonthCloseStore(db)
        db.billDao().insert(
            BillEntity(
                id = "paid",
                name = "Paid",
                amountCents = 5000,
                dueDay = 10,
                category = "utility",
                isPaid = true,
                paidMonth = month.toString(),
                dueMonth = month.toString(),
                createdAt = 1,
                isRecurring = false
            )
        )
        db.billDao().insert(
            BillEntity(
                id = "recurring",
                name = "Recurring",
                amountCents = 2000,
                dueDay = 15,
                category = "utility",
                isPaid = true,
                paidMonth = month.toString(),
                dueMonth = month.toString(),
                createdAt = 2,
                isRecurring = true
            )
        )

        val request = MonthCloseRequest(
            month = month.toString(),
            nextMonth = month.plusMonths(1).toString(),
            history = MonthHistoryRecord(month.toString(), 100000, 7000, 70000, 20000, 10000),
            payments = listOf(payment("paid"), payment("recurring")),
            archivedBillIds = listOf("paid")
        )

        assertTrue(store.close(request))
        assertEquals(false, store.close(request))
        assertEquals(2, db.paymentHistoryDao().getByMonth(month.toString()).size)
        assertEquals(1, db.monthHistoryDao().getAll().size)
        assertEquals(null, db.billDao().getBillById("paid"))
        assertEquals(month.plusMonths(1).toString(), db.billDao().getBillById("recurring")?.dueMonth)
        db.close()
    }

    @Test
    fun roomTransactionRollsBackWhenAnOperationFails() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, VisoDB::class.java).build()
        try {
            try {
                db.withTransaction {
                    db.monthHistoryDao().insert(
                        com.viso.data.db.entity.MonthHistoryEntity(
                            month.toString(), 1, 1, 1, 1, 1
                        )
                    )
                    error("simulated failure between close steps")
                }
            } catch (_: IllegalStateException) {
                // expected
            }

            assertEquals(null, db.monthHistoryDao().getByMonth(month.toString()))
        } finally {
            db.close()
        }
    }

    private fun payment(billId: String) = PaymentHistory(
        id = "payment:${month}:$billId",
        month = month.toString(),
        billId = billId,
        billName = billId,
        amountCents = 100,
        category = "utility",
        dueDay = 1,
        paidAt = 1
    )
}
