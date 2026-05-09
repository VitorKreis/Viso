package com.viso.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.viso.data.db.dao.AchievementDao
import com.viso.data.db.dao.BillDao
import com.viso.data.db.dao.ExtraIncomeDao
import com.viso.data.db.dao.GoalDao
import com.viso.data.db.dao.InstallmentBillDao
import com.viso.data.db.dao.MonthHistoryDao
import com.viso.data.db.entity.AchievementEntity
import com.viso.data.db.entity.BillEntity
import com.viso.data.db.entity.ExtraIncomeEntity
import com.viso.data.db.entity.GoalEntity
import com.viso.data.db.entity.InstallmentBillEntity
import com.viso.data.db.entity.MonthHistoryEntity

@Database(
    entities = [
        BillEntity::class,
        GoalEntity::class,
        ExtraIncomeEntity::class,
        MonthHistoryEntity::class,
        InstallmentBillEntity::class,
        AchievementEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class VisoDB : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun goalDao(): GoalDao
    abstract fun extraIncomeDao(): ExtraIncomeDao
    abstract fun monthHistoryDao(): MonthHistoryDao
    abstract fun installmentBillDao(): InstallmentBillDao
    abstract fun achievementDao(): AchievementDao
}
