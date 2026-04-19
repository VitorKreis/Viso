package com.viso.domain.model

data class Bill(
    val id: String,
    val name: String,
    val amountCents: Long,
    val dueDay: Int,
    val category: String,
    val isPaid: Boolean,
    val paidMonth: String,
    val createdAt: Long
    ,
    val isRecurring: Boolean = false
)
