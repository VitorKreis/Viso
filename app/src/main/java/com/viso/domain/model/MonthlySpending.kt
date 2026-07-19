package com.viso.domain.model

data class MonthlySpending(
    val yearMonth: String,
    val totalCents: Long,
    val label: String
)
