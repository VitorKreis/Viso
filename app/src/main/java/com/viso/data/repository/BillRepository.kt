package com.viso.data.repository

import com.viso.data.db.dao.BillDao
import com.viso.data.db.toDomainBill
import com.viso.data.db.toEntity
import com.viso.domain.model.Bill
import com.viso.domain.model.IdentityContract
import com.viso.domain.model.MonthContract
import com.viso.domain.repository.BillRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepository @Inject constructor(
    private val billDao: BillDao
) : BillRepositoryContract {
    override fun getAllBillsFlow(): Flow<List<Bill>> =
        billDao.getAllBills().map { list -> list.map { it.toDomainBill() } }

    override suspend fun getAllBills(): List<Bill> =
        billDao.getAllBillsList().map { it.toDomainBill() }

    suspend fun getBillById(id: String): Bill? =
        billDao.getBillById(id)?.toDomainBill()

    override suspend fun insert(bill: Bill) =
        // Prevent accidental duplicates: if a bill with the same
        // name/amount/dueDay/category exists, reuse its id and replace it.
        run {
            val existing = billDao.findDuplicate(
                bill.name,
                bill.amountCents,
                bill.dueDay,
                bill.category,
                bill.dueMonth
            )
            val toInsert = if (existing != null) bill.copy(id = existing.id) else bill
            billDao.insert(toInsert.toEntity())
        }

    suspend fun update(bill: Bill) =
        billDao.update(bill.toEntity())

    suspend fun deleteById(id: String) =
        billDao.deleteById(id)

    suspend fun resetAllPaidStatus(month: String) =
        billDao.resetAllPaidStatus(MonthContract.requireMonth(month))

    suspend fun markAsPaid(id: String, month: String) =
        billDao.markAsPaid(
            IdentityContract.requireId(id),
            MonthContract.requireMonth(month, "paidMonth")
        )

    suspend fun markAsUnpaid(id: String) =
        billDao.markAsUnpaid(IdentityContract.requireId(id))

    suspend fun getInstallmentBillsByParentId(parentId: String): List<Bill> =
        billDao.getAllBillsList()
            .filter { it.parentInstallmentId == parentId }
            .map { it.toDomainBill() }

    suspend fun getCategorySpending(month: String): Map<String, Long> {
        return billDao.getCategorySpending(month).associate { it.category to it.total }
    }

    suspend fun resetRecurringPaidStatus(closedMonth: String, nextMonth: String) {
        billDao.resetRecurringPaidStatus(
            MonthContract.requireMonth(closedMonth, "closedMonth"),
            MonthContract.requireMonth(nextMonth, "nextMonth")
        )
    }

}
