package com.viso.data.repository

import com.viso.data.db.dao.ExtraIncomeDao
import com.viso.data.db.entity.ExtraIncomeEntity
import com.viso.domain.model.ExtraIncome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtraIncomeRepository @Inject constructor(
    private val extraIncomeDao: ExtraIncomeDao
) {
    fun getByMonthFlow(month: String): Flow<List<ExtraIncome>> =
        extraIncomeDao.getByMonth(month).map { list -> list.map { it.toDomain() } }

    suspend fun getByMonth(month: String): List<ExtraIncome> =
        extraIncomeDao.getByMonthList(month).map { it.toDomain() }

    suspend fun getTotalForMonth(month: String): Long =
        extraIncomeDao.getTotalForMonth(month)

    fun getTotalForMonthFlow(month: String): Flow<Long> =
        extraIncomeDao.getTotalForMonthFlow(month)

    suspend fun insert(extra: ExtraIncome) =
        extraIncomeDao.insert(extra.toEntity())

    suspend fun deleteById(id: String) =
        extraIncomeDao.deleteById(id)

    suspend fun deleteByMonth(month: String) =
        extraIncomeDao.deleteByMonth(month)

    private fun ExtraIncomeEntity.toDomain() = ExtraIncome(
        id = id,
        name = name,
        amountCents = amountCents,
        month = month
    )

    private fun ExtraIncome.toEntity() = ExtraIncomeEntity(
        id = id,
        name = name,
        amountCents = amountCents,
        month = month
    )
}
