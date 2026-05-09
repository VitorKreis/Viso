package com.viso.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installment_bills")
data class InstallmentBillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val totalAmountCents: Long,
    val installmentAmountCents: Long,
    val totalInstallments: Int,
    val startMonth: String,
    val category: String,
    val dueDay: Int,
    val isActive: Boolean = true,
    val createdAt: Long
)
