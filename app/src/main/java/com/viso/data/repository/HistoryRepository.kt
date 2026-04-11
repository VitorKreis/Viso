package com.viso.data.repository

import com.viso.data.db.dao.MonthHistoryDao
import com.viso.data.db.entity.MonthHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val monthHistoryDao: MonthHistoryDao
) {
    suspend fun saveMonth(entity: MonthHistoryEntity) =
        monthHistoryDao.insert(entity)

    suspend fun getAll(): List<MonthHistoryEntity> =
        monthHistoryDao.getAll()

    suspend fun getByMonth(month: String): MonthHistoryEntity? =
        monthHistoryDao.getByMonth(month)
}
