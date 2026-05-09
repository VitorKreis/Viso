package com.viso.data.repository

import com.viso.data.db.dao.InstallmentBillDao
import com.viso.data.db.entity.InstallmentBillEntity
import com.viso.domain.model.InstallmentBill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallmentBillRepository @Inject constructor(
    private val installmentBillDao: InstallmentBillDao
) {
    fun getAllActiveFlow(): Flow<List<InstallmentBill>> =
        installmentBillDao.getAllActive().map { list -> list.map { it.toDomain() } }

    suspend fun getAllActive(): List<InstallmentBill> =
        installmentBillDao.getAllActiveList().map { it.toDomain() }

    suspend fun getById(id: String): InstallmentBill? =
        installmentBillDao.getById(id)?.toDomain()

    suspend fun insert(installmentBill: InstallmentBill) =
        installmentBillDao.insert(installmentBill.toEntity())

    suspend fun update(installmentBill: InstallmentBill) =
        installmentBillDao.update(installmentBill.toEntity())

    suspend fun deleteById(id: String) =
        installmentBillDao.deleteById(id)

    suspend fun deactivate(id: String) =
        installmentBillDao.deactivate(id)

    private fun InstallmentBillEntity.toDomain() = InstallmentBill(
        id = id,
        name = name,
        totalAmountCents = totalAmountCents,
        installmentAmountCents = installmentAmountCents,
        totalInstallments = totalInstallments,
        startMonth = startMonth,
        category = category,
        dueDay = dueDay,
        isActive = isActive,
        createdAt = createdAt
    )

    private fun InstallmentBill.toEntity() = InstallmentBillEntity(
        id = id,
        name = name,
        totalAmountCents = totalAmountCents,
        installmentAmountCents = installmentAmountCents,
        totalInstallments = totalInstallments,
        startMonth = startMonth,
        category = category,
        dueDay = dueDay,
        isActive = isActive,
        createdAt = createdAt
    )
}
