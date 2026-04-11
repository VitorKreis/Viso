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

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE bills SET isPaid = 0, paidMonth = :month WHERE isPaid = 1")
    suspend fun resetAllPaidStatus(month: String)

    @Query("UPDATE bills SET isPaid = 1, paidMonth = :month WHERE id = :id")
    suspend fun markAsPaid(id: String, month: String)

    @Query("UPDATE bills SET isPaid = 0 WHERE id = :id")
    suspend fun markAsUnpaid(id: String)
}
