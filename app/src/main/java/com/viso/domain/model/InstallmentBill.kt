package com.viso.domain.model

data class InstallmentBill(
    val id: String,
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
