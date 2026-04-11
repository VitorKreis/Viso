package com.viso.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "month_history")
data class MonthHistoryEntity(
    @PrimaryKey val month: String,
    val salaryCents: Long,
    val totalBillsCents: Long,
    val billsLimitCents: Long,
    val spendingBudgetCents: Long,
    val savingsBudgetCents: Long
)
