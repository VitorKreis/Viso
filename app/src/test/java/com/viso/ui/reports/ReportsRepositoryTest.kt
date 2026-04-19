package com.viso.ui.reports

import com.viso.data.db.dao.MonthHistoryDao
import com.viso.data.db.entity.MonthHistoryEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportsRepositoryTest {
    private class FakeMonthHistoryDao(private val items: List<MonthHistoryEntity>) : MonthHistoryDao {
        override suspend fun getAll(): List<MonthHistoryEntity> = items

        override suspend fun getByMonth(month: String): MonthHistoryEntity? = items.find { it.month == month }

        override suspend fun insert(history: MonthHistoryEntity) { /* no-op */ }
    }

    @Test
    fun testMappingLast12Months() = runBlocking {
        val sample = listOf(
            MonthHistoryEntity("2025-12", 100000L, 20000L, 30000L, 40000L, 10000L),
            MonthHistoryEntity("2026-01", 100000L, 25000L, 30000L, 35000L, 15000L),
            MonthHistoryEntity("2026-02", 100000L, 22000L, 30000L, 37000L, 11000L)
        )

        val dao = FakeMonthHistoryDao(sample)
        val repo = ReportsRepository(dao)

        val months = repo.getLast12Months()
        assertEquals(3, months.size)
        assertEquals("2025-12", months[0].month)
        assertEquals(20000L, months[0].billsCents)
        assertEquals(40000L, months[0].spendingCents)
        assertEquals(10000L, months[0].savingsCents)
    }
}
