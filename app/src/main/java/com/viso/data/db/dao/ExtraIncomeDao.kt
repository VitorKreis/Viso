package com.viso.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viso.data.db.entity.ExtraIncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtraIncomeDao {

    @Query("SELECT * FROM extra_incomes WHERE month = :month ORDER BY name ASC")
    fun getByMonth(month: String): Flow<List<ExtraIncomeEntity>>

    @Query("SELECT * FROM extra_incomes WHERE month = :month ORDER BY name ASC")
    suspend fun getByMonthList(month: String): List<ExtraIncomeEntity>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM extra_incomes WHERE month = :month")
    suspend fun getTotalForMonth(month: String): Long

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM extra_incomes WHERE month = :month")
    fun getTotalForMonthFlow(month: String): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extraIncome: ExtraIncomeEntity)

    @Delete
    suspend fun delete(extraIncome: ExtraIncomeEntity)

    @Query("DELETE FROM extra_incomes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM extra_incomes WHERE month = :month")
    suspend fun deleteByMonth(month: String)
}
