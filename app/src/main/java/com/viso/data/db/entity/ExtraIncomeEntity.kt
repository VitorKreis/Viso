package com.viso.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extra_incomes")
data class ExtraIncomeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amountCents: Long,
    val month: String
)
