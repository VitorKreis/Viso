package com.viso.data.repository

import com.viso.data.db.dao.PaymentHistoryDao
import com.viso.data.db.entity.PaymentHistoryEntity
import com.viso.domain.model.PaymentHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentHistoryRepository @Inject constructor(
    private val paymentHistoryDao: PaymentHistoryDao
) {
    suspend fun insert(history: PaymentHistory) = withContext(Dispatchers.IO) {
        paymentHistoryDao.insert(history.toEntity())
    }

    suspend fun getByMonth(month: String): List<PaymentHistory> = withContext(Dispatchers.IO) {
        paymentHistoryDao.getByMonth(month).map { it.toDomain() }
    }

    suspend fun getAll(): List<PaymentHistory> = withContext(Dispatchers.IO) {
        paymentHistoryDao.getAll().map { it.toDomain() }
    }

    suspend fun getMonthlyTotals(): Map<String, Long> = withContext(Dispatchers.IO) {
        paymentHistoryDao.getMonthlyTotals().associate { it.month to it.total }
    }

    suspend fun getCategorySpendingForMonth(month: String): Map<String, Long> = withContext(Dispatchers.IO) {
        paymentHistoryDao.getCategorySpendingForMonth(month).associate { it.category to it.total }
    }

    suspend fun hasHistoryForMonth(month: String): Boolean = withContext(Dispatchers.IO) {
        paymentHistoryDao.hasHistoryForMonth(month)
    }

    suspend fun deleteByMonth(month: String) = withContext(Dispatchers.IO) {
        paymentHistoryDao.deleteByMonth(month)
    }

    private fun PaymentHistoryEntity.toDomain() = PaymentHistory(
        id = id,
        month = month,
        billId = billId,
        billName = billName,
        amountCents = amountCents,
        category = category,
        dueDay = dueDay,
        paidAt = paidAt,
        isRecurring = isRecurring
    )

    private fun PaymentHistory.toEntity() = PaymentHistoryEntity(
        id = id,
        month = month,
        billId = billId,
        billName = billName,
        amountCents = amountCents,
        category = category,
        dueDay = dueDay,
        paidAt = paidAt,
        isRecurring = isRecurring
    )
}
