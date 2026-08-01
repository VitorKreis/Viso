package com.viso.di

import android.content.Context
import androidx.room.Room
import com.viso.data.datastore.ConfigDataStore
import com.viso.data.db.VisoDB
import com.viso.data.db.dao.AchievementDao
import com.viso.data.db.dao.BillDao
import com.viso.data.db.dao.ExtraIncomeDao
import com.viso.data.db.dao.GoalDao
import com.viso.data.db.dao.InstallmentBillDao
import com.viso.data.db.dao.MonthHistoryDao
import com.viso.data.db.dao.PaymentHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VisoDB =
        Room.databaseBuilder(context, VisoDB::class.java, "viso.db")
            .addMigrations(object : androidx.room.migration.Migration(1, 2) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE bills ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                    db.execSQL(
                        "INSERT OR REPLACE INTO goals (id, name, targetAmountCents, currentAmountCents, monthlyContributionCents, isEmergencyFund, color, createdAt) " +
                                "SELECT 'emergency_fund', name, targetAmountCents, currentAmountCents, monthlyContributionCents, isEmergencyFund, color, createdAt " +
                                "FROM goals WHERE isEmergencyFund = 1 ORDER BY createdAt ASC LIMIT 1"
                    )
                    db.execSQL("DELETE FROM goals WHERE isEmergencyFund = 1 AND id <> 'emergency_fund'")
                }
            })
            .addMigrations(object : androidx.room.migration.Migration(2, 3) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Create installment_bills table
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
                    // Add installment columns to bills table
                    db.execSQL("ALTER TABLE bills ADD COLUMN isInstallment INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE bills ADD COLUMN installmentNumber INTEGER")
                    db.execSQL("ALTER TABLE bills ADD COLUMN totalInstallments INTEGER")
                    db.execSQL("ALTER TABLE bills ADD COLUMN parentInstallmentId TEXT")
                }
            })
            .addMigrations(object : androidx.room.migration.Migration(3, 4) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Create achievements table
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
            })
            .addMigrations(object : androidx.room.migration.Migration(4, 5) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Create payment_history table
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
                    // Create index on month for fast queries
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_history_month ON payment_history(month)")
                }
            })
            .addMigrations(object : androidx.room.migration.Migration(5, 6) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE bills ADD COLUMN dueMonth TEXT NOT NULL DEFAULT ''")
                }
            })
            .build()

    @Provides
    fun provideBillDao(db: VisoDB): BillDao = db.billDao()

    @Provides
    fun provideGoalDao(db: VisoDB): GoalDao = db.goalDao()

    @Provides
    fun provideExtraIncomeDao(db: VisoDB): ExtraIncomeDao = db.extraIncomeDao()

    @Provides
    fun provideMonthHistoryDao(db: VisoDB): MonthHistoryDao = db.monthHistoryDao()

    @Provides
    fun provideInstallmentBillDao(db: VisoDB): InstallmentBillDao = db.installmentBillDao()

    @Provides
    fun provideAchievementDao(db: VisoDB): AchievementDao = db.achievementDao()

    @Provides
    fun providePaymentHistoryDao(db: VisoDB): PaymentHistoryDao = db.paymentHistoryDao()

    @Provides
    @Singleton
    fun provideConfigDataStore(@ApplicationContext context: Context): ConfigDataStore =
        ConfigDataStore(context)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth =
        com.google.firebase.auth.FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
}
