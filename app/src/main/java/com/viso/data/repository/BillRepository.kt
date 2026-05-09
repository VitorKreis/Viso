package com.viso.data.repository

import com.viso.data.db.dao.BillDao
import com.viso.data.db.entity.BillEntity
import com.viso.domain.model.Bill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepository @Inject constructor(
    private val billDao: BillDao
) {
    fun getAllBillsFlow(): Flow<List<Bill>> =
        billDao.getAllBills().map { list -> list.map { it.toDomain() } }

    suspend fun getAllBills(): List<Bill> =
        billDao.getAllBillsList().map { it.toDomain() }

    suspend fun getBillById(id: String): Bill? =
        billDao.getBillById(id)?.toDomain()

    suspend fun insert(bill: Bill) =
        // Prevent accidental duplicates: if a bill with the same
        // name/amount/dueDay/category exists, reuse its id and replace it.
        run {
            val existing = billDao.findDuplicate(bill.name, bill.amountCents, bill.dueDay, bill.category)
            val toInsert = if (existing != null) bill.copy(id = existing.id) else bill
            billDao.insert(toInsert.toEntity())
        }

    suspend fun update(bill: Bill) =
        billDao.update(bill.toEntity())

    suspend fun deleteById(id: String) =
        billDao.deleteById(id)

    suspend fun resetAllPaidStatus(month: String) =
        billDao.resetAllPaidStatus(month)

    suspend fun markAsPaid(id: String, month: String) =
        billDao.markAsPaid(id, month)

    suspend fun markAsUnpaid(id: String) =
        billDao.markAsUnpaid(id)

    suspend fun getInstallmentBillsByParentId(parentId: String): List<Bill> =
        billDao.getAllBillsList()
            .filter { it.parentInstallmentId == parentId }
            .map { it.toDomain() }

    private fun BillEntity.toDomain() = Bill(
        id = id,
        name = name,
        amountCents = amountCents,
        dueDay = dueDay,
        category = category,
        isPaid = isPaid,
        paidMonth = paidMonth,
        createdAt = createdAt,
        isRecurring = isRecurring,
        isInstallment = isInstallment,
        installmentNumber = installmentNumber,
        totalInstallments = totalInstallments,
        parentInstallmentId = parentInstallmentId
    )

    private fun Bill.toEntity() = BillEntity(
        id = id,
        name = name,
        amountCents = amountCents,
        dueDay = dueDay,
        category = category,
        isPaid = isPaid,
        paidMonth = paidMonth,
        createdAt = createdAt,
        isRecurring = isRecurring,
        isInstallment = isInstallment,
        installmentNumber = installmentNumber,
        totalInstallments = totalInstallments,
        parentInstallmentId = parentInstallmentId
    )
}
