package com.viso.domain.model

data class Goal(
    val id: String,
    val name: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long,
    val monthlyContributionCents: Long,
    val isEmergencyFund: Boolean,
    val color: String,
    val createdAt: Long
)
