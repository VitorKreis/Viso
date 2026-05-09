package com.viso.domain.model

enum class Rarity {
    COMMON, RARE, EPIC, LEGENDARY
}

enum class AchievementType {
    STREAK, MILESTONE, SAVING
}

data class Achievement(
    val id: String,
    val type: AchievementType,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val progress: Int,
    val target: Int,
    val rarity: Rarity
) {
    val progressPercentage: Float
        get() = if (target > 0) progress.toFloat() / target.toFloat() else 0f
}

data class StreakInfo(
    val currentStreak: Int,
    val maxStreak: Int,
    val lastMonthCompleted: Boolean,
    val thisMonthProgress: Float,
    val daysRemaining: Int
)
