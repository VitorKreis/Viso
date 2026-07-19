package com.viso.domain.model

data class PaymentHistory(
    val id: String,
    val month: String,
    val billId: String,
    val billName: String,
    val amountCents: Long,
    val category: String,
    val dueDay: Int,
    val paidAt: Long,
    val isRecurring: Boolean = false
)
