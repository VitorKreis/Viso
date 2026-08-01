package com.viso.domain.usecase

import com.viso.data.repository.BillRepository
import com.viso.domain.model.Bill
import java.time.LocalDate
import javax.inject.Inject

class GetUpcomingBillsUseCase @Inject constructor(
    private val billRepo: BillRepository
) {
    suspend operator fun invoke(days: Int = 7): List<Bill> {
        val today = LocalDate.now()
        val bills = billRepo.getAllBills()
        return bills.filter { bill ->
            if (bill.isPaid) return@filter false
            val dueDate = billDueDate(bill, java.time.YearMonth.from(today))
            dueDate in today..today.plusDays(days.toLong())
        }
    }
}
