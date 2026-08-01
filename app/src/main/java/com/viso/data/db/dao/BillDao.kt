package com.viso.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.viso.data.db.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Query("SELECT * FROM bills ORDER BY dueDay ASC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills ORDER BY dueDay ASC")
    suspend fun getAllBillsList(): List<BillEntity>

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: String): BillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE name = :name AND amountCents = :amountCents AND dueDay = :dueDay AND category = :category AND dueMonth = :dueMonth LIMIT 1")
    suspend fun findDuplicate(name: String, amountCents: Long, dueDay: Int, category: String, dueMonth: String): BillEntity?

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE bills SET isPaid = 0, paidMonth = '', dueMonth = :month WHERE isPaid = 1")
    suspend fun resetAllPaidStatus(month: String)

    @Query("UPDATE bills SET isPaid = 1, paidMonth = :month WHERE id = :id")
    suspend fun markAsPaid(id: String, month: String)

    @Query("UPDATE bills SET isPaid = 0 WHERE id = :id")
    suspend fun markAsUnpaid(id: String)

    @Query("UPDATE bills SET isPaid = 0, paidMonth = '', dueMonth = :nextMonth WHERE isRecurring = 1 AND isPaid = 1 AND (dueMonth = :closedMonth OR dueMonth = '')")
    suspend fun resetRecurringPaidStatus(closedMonth: String, nextMonth: String)

    @Query("SELECT category, SUM(amountCents) as total FROM bills WHERE paidMonth = :month GROUP BY category")
    suspend fun getCategorySpending(month: String): List<com.viso.data.db.entity.CategorySpendTuple>

    @Query("SELECT paidMonth as paidMonth, SUM(amountCents) as totalCents FROM bills WHERE isPaid = 1 AND paidMonth != '' GROUP BY paidMonth ORDER BY paidMonth")
    suspend fun getMonthlySpending(): List<MonthSpendingTuple>
}

data class MonthSpendingTuple(
    val paidMonth: String,
    val totalCents: Long
)
