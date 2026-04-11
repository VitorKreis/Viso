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
        billDao.insert(bill.toEntity())

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

    private fun BillEntity.toDomain() = Bill(
        id = id,
        name = name,
        amountCents = amountCents,
        dueDay = dueDay,
        category = category,
        isPaid = isPaid,
        paidMonth = paidMonth,
        createdAt = createdAt
    )

    private fun Bill.toEntity() = BillEntity(
        id = id,
        name = name,
        amountCents = amountCents,
        dueDay = dueDay,
        category = category,
        isPaid = isPaid,
        paidMonth = paidMonth,
        createdAt = createdAt
    )
}
