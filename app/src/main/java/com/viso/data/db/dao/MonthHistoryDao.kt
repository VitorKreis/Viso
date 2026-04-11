package com.viso.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viso.data.db.entity.MonthHistoryEntity

@Dao
interface MonthHistoryDao {

    @Query("SELECT * FROM month_history ORDER BY month DESC")
    suspend fun getAll(): List<MonthHistoryEntity>

    @Query("SELECT * FROM month_history WHERE month = :month")
    suspend fun getByMonth(month: String): MonthHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: MonthHistoryEntity)
}
