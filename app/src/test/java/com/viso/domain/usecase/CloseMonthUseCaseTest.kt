package com.viso.domain.usecase

import com.viso.domain.model.Bill
import com.viso.domain.model.Config
import com.viso.domain.model.PaymentHistory
import com.viso.domain.repository.ConfigRepositoryContract
import com.viso.domain.repository.MonthCloseRequest
import com.viso.domain.repository.MonthCloseSideEffects
import com.viso.domain.repository.MonthCloseStore
import com.viso.domain.repository.MonthHistoryRecord
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloseMonthUseCaseTest {
    private val month = YearMonth.of(2026, 4)

    @Test
    fun normalCloseArchivesOnlyPaidOneOffBills() = runBlocking {
        val store = FakeMonthCloseStore(
            bills = listOf(
                bill("paid", month, isPaid = true),
                bill("pending", month, isPaid = false),
                bill("future", month.plusMonths(1), isPaid = true)
            ),
            extraIncomeCents = 1250
        )
        val effects = FakeSideEffects()

        val result = createUseCase(store, effects).invoke(month)

        assertEquals(20000L, result.totalBillsCents)
        assertEquals(1, result.paidBillsCount)
        assertEquals(1, result.unpaidBillsCount)
        assertEquals(1, result.archivedBillsCount)
        assertEquals(1250, result.extraIncomeCents)
        assertEquals(1, store.closeCalls)
        assertEquals(1, effects.successfulCloses)
        assertEquals(false, result.monthCompleted)
    }

    @Test
    fun monthWithoutBillsIsNotMarkedCompleted() = runBlocking {
        val store = FakeMonthCloseStore()

        val result = createUseCase(store, FakeSideEffects()).invoke(month)

        assertEquals(0, result.paidBillsCount)
        assertEquals(0, result.unpaidBillsCount)
        assertEquals(false, result.monthCompleted)
        assertEquals(1, store.closeCalls)
    }

    @Test
    fun futureBillsDoNotBelongToClosedMonth() = runBlocking {
        val store = FakeMonthCloseStore(
            bills = listOf(bill("future", month.plusMonths(1), isPaid = false))
        )

        val result = createUseCase(store, FakeSideEffects()).invoke(month)

        assertEquals(0, result.unpaidBillsCount)
        assertTrue(store.requests.single().payments.isEmpty())
    }

    @Test
    fun pendingBillsRemainInResultAndBlockCompletion() = runBlocking {
        val store = FakeMonthCloseStore(
            bills = listOf(bill("pending", month, isPaid = false))
        )

        val result = createUseCase(store, FakeSideEffects()).invoke(month)

        assertEquals(1, result.unpaidBillsCount)
        assertEquals(false, result.monthCompleted)
        assertTrue(store.requests.single().archivedBillIds.isEmpty())
    }

    @Test
    fun advancePaymentIsRecordedForItsDueMonth() = runBlocking {
        val store = FakeMonthCloseStore(
            bills = listOf(
                bill("advance", month, isPaid = true, paidMonth = month.minusMonths(1).toString())
            )
        )

        val result = createUseCase(store, FakeSideEffects()).invoke(month)

        assertEquals(1, result.paidBillsCount)
        assertEquals(month.toString(), store.requests.single().month)
        assertEquals(month.minusMonths(1).toString(), store.bills.single().paidMonth)
    }

    @Test
    fun repeatedCloseUsesHistoryWithoutWritingAgain() = runBlocking {
        val store = FakeMonthCloseStore(bills = listOf(bill("paid", month, isPaid = true)))
        val useCase = createUseCase(store, FakeSideEffects())

        useCase.invoke(month)
        val second = useCase.invoke(month)

        assertEquals(1, store.closeCalls)
        assertEquals(1, store.requests.size)
        assertEquals(1, second.paidBillsCount)
    }

    @Test
    fun failureDuringPersistenceDoesNotRunSideEffects() = runBlocking {
        val store = FakeMonthCloseStore(
            bills = listOf(bill("paid", month, isPaid = true)),
            failOnClose = true
        )
        val effects = FakeSideEffects()

        var failed = false
        try {
            createUseCase(store, effects).invoke(month)
        } catch (_: IllegalStateException) {
            // expected: the Room implementation uses the same transaction
            // boundary and rolls back writes before this point.
            failed = true
        }

        assertTrue(failed)
        assertEquals(0, effects.successfulCloses)
        assertTrue(store.history.isEmpty())
    }

    private fun createUseCase(store: FakeMonthCloseStore, effects: FakeSideEffects) =
        CloseMonthUseCase(FakeConfigRepository(), store, effects)

    private fun bill(
        id: String,
        dueMonth: YearMonth,
        isPaid: Boolean,
        paidMonth: String = if (isPaid) dueMonth.toString() else ""
    ) = Bill(
        id = id,
        name = id,
        amountCents = 10000,
        dueDay = 10,
        category = "other",
        isPaid = isPaid,
        paidMonth = paidMonth,
        dueMonth = dueMonth.toString(),
        createdAt = 1
    )

    private class FakeConfigRepository : ConfigRepositoryContract {
        var config = Config(salaryCents = 100000, lastResetMonth = "")

        override suspend fun getConfig(): Config = config

        override suspend fun updateLastResetMonth(month: String) {
            config = config.copy(lastResetMonth = month)
        }
    }

    private class FakeSideEffects : MonthCloseSideEffects {
        var successfulCloses = 0

        override suspend fun onAlreadyClosed() = Unit

        override suspend fun onSuccessfulClose(monthCompleted: Boolean) {
            successfulCloses++
        }
    }

    private class FakeMonthCloseStore(
        val bills: List<Bill> = emptyList(),
        private val extraIncomeCents: Long = 0,
        private val failOnClose: Boolean = false
    ) : MonthCloseStore {
        val history = mutableMapOf<String, MonthHistoryRecord>()
        val payments = mutableMapOf<String, List<PaymentHistory>>()
        val requests = mutableListOf<MonthCloseRequest>()
        var closeCalls = 0

        override suspend fun getBills(): List<Bill> = bills

        override suspend fun getExtraIncomeTotal(month: String): Long = extraIncomeCents

        override suspend fun getHistory(month: String): MonthHistoryRecord? = history[month]

        override suspend fun getPayments(month: String): List<PaymentHistory> = payments[month].orEmpty()

        override suspend fun close(request: MonthCloseRequest): Boolean {
            closeCalls++
            if (failOnClose) throw IllegalStateException("simulated persistence failure")
            if (history.containsKey(request.month)) return false
            requests += request
            history[request.month] = request.history
            payments[request.month] = request.payments
            return true
        }
    }
}
