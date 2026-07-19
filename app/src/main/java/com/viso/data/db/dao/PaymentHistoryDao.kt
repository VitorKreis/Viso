package com.viso.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viso.data.db.entity.CategorySpendTuple
import com.viso.data.db.entity.PaymentHistoryEntity

@Dao
interface PaymentHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PaymentHistoryEntity)

    @Query("SELECT * FROM payment_history WHERE month = :month ORDER BY paidAt DESC")
    suspend fun getByMonth(month: String): List<PaymentHistoryEntity>

    @Query("SELECT * FROM payment_history ORDER BY month DESC, paidAt DESC")
    suspend fun getAll(): List<PaymentHistoryEntity>

    @Query("SELECT month, SUM(amountCents) as total FROM payment_history GROUP BY month ORDER BY month DESC")
    suspend fun getMonthlyTotals(): List<MonthTotalTuple>

    @Query("SELECT category, SUM(amountCents) as total FROM payment_history WHERE month = :month GROUP BY category")
    suspend fun getCategorySpendingForMonth(month: String): List<CategorySpendTuple>

    @Query("DELETE FROM payment_history WHERE month = :month")
    suspend fun deleteByMonth(month: String)

    @Query("SELECT EXISTS(SELECT 1 FROM payment_history WHERE month = :month LIMIT 1)")
    suspend fun hasHistoryForMonth(month: String): Boolean
}

data class MonthTotalTuple(
    val month: String,
    val total: Long
)
