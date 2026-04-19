package com.viso.di

import android.content.Context
import androidx.room.Room
import com.viso.data.datastore.ConfigDataStore
import com.viso.data.db.VisoDB
import com.viso.data.db.dao.BillDao
import com.viso.data.db.dao.ExtraIncomeDao
import com.viso.data.db.dao.GoalDao
import com.viso.data.db.dao.MonthHistoryDao
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
        // Migration to add `isRecurring` column to `bills` table (default false)
        Room.databaseBuilder(context, VisoDB::class.java, "viso.db")
            .addMigrations(object : androidx.room.migration.Migration(1, 2) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE bills ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                    // Consolidate any duplicate emergency fund goals into a single row with fixed id
                    database.execSQL(
                        "INSERT OR REPLACE INTO goals (id, name, targetAmountCents, currentAmountCents, monthlyContributionCents, isEmergencyFund, color, createdAt) " +
                                "SELECT 'emergency_fund', name, targetAmountCents, currentAmountCents, monthlyContributionCents, isEmergencyFund, color, createdAt " +
                                "FROM goals WHERE isEmergencyFund = 1 ORDER BY createdAt ASC LIMIT 1"
                    )
                    database.execSQL("DELETE FROM goals WHERE isEmergencyFund = 1 AND id <> 'emergency_fund'")
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
    @Singleton
    fun provideConfigDataStore(@ApplicationContext context: Context): ConfigDataStore =
        ConfigDataStore(context)
}
