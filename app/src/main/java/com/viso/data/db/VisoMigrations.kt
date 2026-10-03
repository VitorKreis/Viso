package com.viso.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Versioned, named migrations shared by production and migration tests. */
object VisoMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE bills ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")

            // Keep the legacy emergency-fund rows. Older code deleted all but
            // one row, which could silently lose user data during an upgrade.
            db.execSQL(
                "INSERT OR IGNORE INTO goals (id, name, targetAmountCents, currentAmountCents, " +
                    "monthlyContributionCents, isEmergencyFund, color, createdAt) " +
                    "SELECT 'emergency_fund', name, targetAmountCents, currentAmountCents, " +
                    "monthlyContributionCents, isEmergencyFund, color, createdAt " +
                    "FROM goals WHERE isEmergencyFund = 1 ORDER BY createdAt ASC LIMIT 1"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS installment_bills (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "totalAmountCents INTEGER NOT NULL, " +
                    "installmentAmountCents INTEGER NOT NULL, " +
                    "totalInstallments INTEGER NOT NULL, " +
                    "startMonth TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "dueDay INTEGER NOT NULL, " +
                    "isActive INTEGER NOT NULL DEFAULT 1, " +
                    "createdAt INTEGER NOT NULL)"
            )
            db.execSQL("ALTER TABLE bills ADD COLUMN isInstallment INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE bills ADD COLUMN installmentNumber INTEGER")
            db.execSQL("ALTER TABLE bills ADD COLUMN totalInstallments INTEGER")
            db.execSQL("ALTER TABLE bills ADD COLUMN parentInstallmentId TEXT")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS achievements (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "icon TEXT NOT NULL, " +
                    "unlockedAt INTEGER, " +
                    "progress INTEGER NOT NULL DEFAULT 0, " +
                    "target INTEGER NOT NULL, " +
                    "rarity TEXT NOT NULL, " +
                    "createdAt INTEGER NOT NULL)"
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS payment_history (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "month TEXT NOT NULL, " +
                    "billId TEXT NOT NULL, " +
                    "billName TEXT NOT NULL, " +
                    "amountCents INTEGER NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "dueDay INTEGER NOT NULL, " +
                    "paidAt INTEGER NOT NULL, " +
                    "isRecurring INTEGER NOT NULL DEFAULT 0)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_history_month ON payment_history(month)")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE bills ADD COLUMN dueMonth TEXT NOT NULL DEFAULT ''")
        }
    }

    val ALL = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6
    )
}
