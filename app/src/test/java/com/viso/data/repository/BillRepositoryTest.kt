package com.viso.data.repository

import com.viso.data.db.dao.BillDao
import com.viso.data.db.entity.BillEntity
import com.viso.domain.model.Bill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BillRepositoryTest {

    private class FakeBillDao : BillDao {
        private val items = mutableListOf<BillEntity>()
        private val flow = MutableStateFlow<List<BillEntity>>(emptyList())

        override fun getAllBills(): Flow<List<BillEntity>> = flow

        override suspend fun getAllBillsList(): List<BillEntity> = items.toList()

        override suspend fun getBillById(id: String): BillEntity? = items.find { it.id == id }

        override suspend fun insert(bill: BillEntity) {
            // replace by id
            items.removeAll { it.id == bill.id }
            items.add(bill)
            flow.value = items.toList()
        }

        override suspend fun update(bill: BillEntity) {
            val idx = items.indexOfFirst { it.id == bill.id }
            if (idx >= 0) items[idx] = bill
            flow.value = items.toList()
        }

        override suspend fun delete(bill: BillEntity) {
            items.removeAll { it.id == bill.id }
            flow.value = items.toList()
        }

        override suspend fun deleteById(id: String) {
            items.removeAll { it.id == id }
            flow.value = items.toList()
        }

        override suspend fun resetAllPaidStatus(month: String) {
            items.replaceAll { it.copy(isPaid = false, paidMonth = month) }
            flow.value = items.toList()
        }

        override suspend fun markAsPaid(id: String, month: String) {
            items.replaceAll { if (it.id == id) it.copy(isPaid = true, paidMonth = month) else it }
            flow.value = items.toList()
        }

        override suspend fun markAsUnpaid(id: String) {
            items.replaceAll { if (it.id == id) it.copy(isPaid = false, paidMonth = "") else it }
            flow.value = items.toList()
        }

        override suspend fun findDuplicate(name: String, amountCents: Long, dueDay: Int, category: String): BillEntity? {
            return items.find { it.name == name && it.amountCents == amountCents && it.dueDay == dueDay && it.category == category }
        }
    }

    @Test
    fun testInsertDeduplicates() = runBlocking {
        val dao = FakeBillDao()
        val repo = BillRepository(dao)

        val bill1 = Bill(
            id = "id-1",
            name = "Internet",
            amountCents = 5000L,
            dueDay = 15,
            category = "utilidade",
            isPaid = false,
            paidMonth = "",
            createdAt = 1L
        )

        val bill2 = Bill(
            id = "id-2",
            name = "Internet",
            amountCents = 5000L,
            dueDay = 15,
            category = "utilidade",
            isPaid = false,
            paidMonth = "",
            createdAt = 2L
        )

        // insert first
        repo.insert(bill1)
        var all = repo.getAllBills()
        assertEquals(1, all.size)
        assertEquals("id-1", all[0].id)

        // insert duplicate-signature bill (different id)
        repo.insert(bill2)
        all = repo.getAllBills()

        // should have deduplicated to a single bill and reused existing id
        assertEquals(1, all.size)
        assertEquals("id-1", all[0].id)
        // amount should be the latest inserted (replace semantics)
        assertEquals(5000L, all[0].amountCents)
    }

    @Test
    fun testRecurringFlagPersisted() = runBlocking {
        val dao = FakeBillDao()
        val repo = BillRepository(dao)

        val bill = Bill(
            id = "r-1",
            name = "Gym",
            amountCents = 3000L,
            dueDay = 5,
            category = "fitness",
            isPaid = false,
            paidMonth = "",
            createdAt = 1L,
            isRecurring = true
        )

        repo.insert(bill)
        val all = repo.getAllBills()
        assertEquals(1, all.size)
        assertEquals(true, all[0].isRecurring)
    }
}
