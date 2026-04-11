package com.viso.data.repository

import com.viso.data.db.dao.GoalDao
import com.viso.data.db.entity.GoalEntity
import com.viso.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun getAllGoalsFlow(): Flow<List<Goal>> =
        goalDao.getAllGoals().map { list -> list.map { it.toDomain() } }

    suspend fun getAllGoals(): List<Goal> =
        goalDao.getAllGoalsList().map { it.toDomain() }

    suspend fun getGoalById(id: String): Goal? =
        goalDao.getGoalById(id)?.toDomain()

    suspend fun getEmergencyFund(): Goal? =
        goalDao.getEmergencyFund()?.toDomain()

    suspend fun getGoalCount(): Int =
        goalDao.getGoalCount()

    suspend fun insert(goal: Goal) =
        goalDao.insert(goal.toEntity())

    suspend fun update(goal: Goal) =
        goalDao.update(goal.toEntity())

    suspend fun deleteById(id: String) =
        goalDao.deleteById(id)

    private fun GoalEntity.toDomain() = Goal(
        id = id,
        name = name,
        targetAmountCents = targetAmountCents,
        currentAmountCents = currentAmountCents,
        monthlyContributionCents = monthlyContributionCents,
        isEmergencyFund = isEmergencyFund,
        color = color,
        createdAt = createdAt
    )

    private fun Goal.toEntity() = GoalEntity(
        id = id,
        name = name,
        targetAmountCents = targetAmountCents,
        currentAmountCents = currentAmountCents,
        monthlyContributionCents = monthlyContributionCents,
        isEmergencyFund = isEmergencyFund,
        color = color,
        createdAt = createdAt
    )
}
