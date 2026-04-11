package com.viso.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.viso.data.db.dao.BillDao
import com.viso.data.db.dao.ExtraIncomeDao
import com.viso.data.db.dao.GoalDao
import com.viso.data.db.dao.MonthHistoryDao
import com.viso.data.db.entity.BillEntity
import com.viso.data.db.entity.ExtraIncomeEntity
import com.viso.data.db.entity.GoalEntity
import com.viso.data.db.entity.MonthHistoryEntity

@Database(
    entities = [
        BillEntity::class,
        GoalEntity::class,
        ExtraIncomeEntity::class,
        MonthHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VisoDB : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun goalDao(): GoalDao
    abstract fun extraIncomeDao(): ExtraIncomeDao
    abstract fun monthHistoryDao(): MonthHistoryDao
}
