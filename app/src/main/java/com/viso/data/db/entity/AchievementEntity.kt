package com.viso.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val type: String,           // "STREAK", "MILESTONE", "SAVING"
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long?,      // null = bloqueada
    val progress: Int,          // progresso atual
    val target: Int,            // meta para desbloquear
    val rarity: String,         // "COMMON", "RARE", "EPIC", "LEGENDARY"
    val createdAt: Long
)
