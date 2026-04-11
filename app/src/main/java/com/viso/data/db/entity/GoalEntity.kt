package com.viso.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long,
    val monthlyContributionCents: Long,
    val isEmergencyFund: Boolean,
    val color: String,
    val createdAt: Long
)
