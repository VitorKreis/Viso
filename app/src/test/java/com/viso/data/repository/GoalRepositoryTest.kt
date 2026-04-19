package com.viso.data.repository

import com.viso.data.db.dao.GoalDao
import com.viso.data.db.entity.GoalEntity
import com.viso.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class GoalRepositoryTest {

    private class FakeGoalDao : GoalDao {
        private val items = mutableListOf<GoalEntity>()
        private val flow = MutableStateFlow<List<GoalEntity>>(emptyList())

        override fun getAllGoals(): Flow<List<GoalEntity>> = flow

        override suspend fun getAllGoalsList(): List<GoalEntity> = items.toList()

        override suspend fun getGoalById(id: String): GoalEntity? = items.find { it.id == id }

        override suspend fun getEmergencyFund(): GoalEntity? = items.find { it.isEmergencyFund }

        override suspend fun getGoalCount(): Int = items.size

        override suspend fun insert(goal: GoalEntity) {
            items.add(goal)
            flow.value = items.toList()
        }

        override suspend fun update(goal: GoalEntity) {
            val idx = items.indexOfFirst { it.id == goal.id }
            if (idx >= 0) items[idx] = goal
            flow.value = items.toList()
        }

        override suspend fun delete(goal: GoalEntity) {
            items.removeAll { it.id == goal.id }
            flow.value = items.toList()
        }

        override suspend fun deleteById(id: String) {
            items.removeAll { it.id == id }
            flow.value = items.toList()
        }
    }

    @Test
    fun testGoalInsertUpdateDelete() = runBlocking {
        val dao = FakeGoalDao()
        val repo = GoalRepository(dao)

        val g = Goal(
            id = "g1",
            name = "Vacation",
            targetAmountCents = 100000L,
            currentAmountCents = 20000L,
            monthlyContributionCents = 10000L,
            isEmergencyFund = false,
            color = "blue",
            createdAt = 1L
        )

        repo.insert(g)
        var all = repo.getAllGoals()
        assertEquals(1, all.size)
        assertEquals("g1", all[0].id)

        // update current amount
        repo.update(g.copy(currentAmountCents = 30000L))
        all = repo.getAllGoals()
        assertEquals(30000L, all[0].currentAmountCents)

        // delete
        repo.deleteById("g1")
        all = repo.getAllGoals()
        assertEquals(0, all.size)
    }
}
