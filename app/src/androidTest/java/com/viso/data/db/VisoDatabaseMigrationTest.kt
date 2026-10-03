package com.viso.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisoDatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        VisoDB::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrateV1ThroughV6_preservesExistingFinancialData() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL(
                "INSERT INTO bills (id, name, amountCents, dueDay, category, isPaid, paidMonth, createdAt) " +
                    "VALUES ('bill-1', 'Internet', 5000, 31, 'utility', 1, '2026-03', 1)"
            )
            execSQL(
                "INSERT INTO goals (id, name, targetAmountCents, currentAmountCents, monthlyContributionCents, " +
                    "isEmergencyFund, color, createdAt) VALUES " +
                    "('goal-1', 'Reserva antiga', 100000, 1000, 500, 1, 'blue', 1), " +
                    "('goal-2', 'Viagem', 200000, 2000, 1000, 0, 'green', 2)"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            DB_NAME,
            6,
            true,
            *VisoMigrations.ALL
        )

        migrated.query("SELECT id, dueMonth FROM bills WHERE id = 'bill-1'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("dueMonth")))
        }
        migrated.query("SELECT COUNT(*) FROM goals").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(3, cursor.getInt(0))
        }
        migrated.query("SELECT COUNT(*) FROM payment_history").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun cleanDatabaseCreation_hasAllCurrentDaos() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, VisoDB::class.java).build()
        assertNotNull(db.billDao())
        assertNotNull(db.goalDao())
        assertNotNull(db.extraIncomeDao())
        assertNotNull(db.monthHistoryDao())
        assertNotNull(db.paymentHistoryDao())
        assertEquals(0, db.billDao().getAllBillsList().size)
        db.close()
    }

    companion object {
        private const val DB_NAME = "migration-test.db"
    }
}
