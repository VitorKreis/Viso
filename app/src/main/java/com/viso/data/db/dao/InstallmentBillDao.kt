package com.viso.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.viso.data.db.entity.InstallmentBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentBillDao {

    @Query("SELECT * FROM installment_bills WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActive(): Flow<List<InstallmentBillEntity>>

    @Query("SELECT * FROM installment_bills WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getAllActiveList(): List<InstallmentBillEntity>

    @Query("SELECT * FROM installment_bills WHERE id = :id")
    suspend fun getById(id: String): InstallmentBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installmentBill: InstallmentBillEntity)

    @Update
    suspend fun update(installmentBill: InstallmentBillEntity)

    @Delete
    suspend fun delete(installmentBill: InstallmentBillEntity)

    @Query("DELETE FROM installment_bills WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE installment_bills SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)
}
