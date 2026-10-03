package com.viso.domain.model

import com.viso.domain.usecase.billDueDate
import com.viso.domain.usecase.billDueMonth
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MonthContractTest {
    @Test
    fun explicitDueMonthWinsOverAdvancePaymentMonth() {
        val bill = Bill(
            id = "bill-1",
            name = "Internet",
            amountCents = 5000,
            dueDay = 31,
            category = "utility",
            isPaid = true,
            paidMonth = "2026-02",
            dueMonth = "2026-03",
            createdAt = 1
        )

        assertEquals(YearMonth.of(2026, 3), billDueMonth(bill))
        assertEquals(LocalDate.of(2026, 3, 31), billDueDate(bill))
    }

    @Test
    fun blankDueMonthUsesLegacyPaidMonthThenFallback() {
        val paidLegacyBill = Bill(
            id = "bill-1",
            name = "Legacy",
            amountCents = 100,
            dueDay = 1,
            category = "other",
            isPaid = true,
            paidMonth = "2026-02",
            createdAt = 1
        )
        val unpaidBill = paidLegacyBill.copy(id = "bill-2", isPaid = false, paidMonth = "")

        assertEquals(YearMonth.of(2026, 2), billDueMonth(paidLegacyBill))
        assertEquals(YearMonth.of(2026, 4), billDueMonth(unpaidBill, YearMonth.of(2026, 4)))
    }

    @Test
    fun invalidMonthIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            MonthContract.requireMonth("2026-13")
        }
    }

    @Test
    fun unpaidBillCannotKeepPaidMonth() {
        assertThrows(IllegalArgumentException::class.java) {
            Bill(
                id = "bill-1",
                name = "Invalid",
                amountCents = 100,
                dueDay = 1,
                category = "other",
                isPaid = false,
                paidMonth = "2026-04",
                createdAt = 1
            )
        }
    }
}
