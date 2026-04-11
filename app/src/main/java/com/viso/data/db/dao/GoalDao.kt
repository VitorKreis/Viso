package com.viso.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.viso.data.db.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY isEmergencyFund DESC, createdAt ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY isEmergencyFund DESC, createdAt ASC")
    suspend fun getAllGoalsList(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: String): GoalEntity?

    @Query("SELECT * FROM goals WHERE isEmergencyFund = 1 LIMIT 1")
    suspend fun getEmergencyFund(): GoalEntity?

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun getGoalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id AND isEmergencyFund = 0")
    suspend fun deleteById(id: String)
}
