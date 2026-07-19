package com.viso.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_history",
    indices = [Index(value = ["month"])]
)
data class PaymentHistoryEntity(
    @PrimaryKey val id: String,
    val month: String,
    val billId: String,
    val billName: String,
    val amountCents: Long,
    val category: String,
    val dueDay: Int,
    val paidAt: Long,
    val isRecurring: Boolean = false
)
