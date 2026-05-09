package com.viso.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amountCents: Long,
    val dueDay: Int,
    val category: String,
    val isPaid: Boolean,
    val paidMonth: String,
    val createdAt: Long,
    val isRecurring: Boolean = false,
    val isInstallment: Boolean = false,
    val installmentNumber: Int? = null,
    val totalInstallments: Int? = null,
    val parentInstallmentId: String? = null
)
