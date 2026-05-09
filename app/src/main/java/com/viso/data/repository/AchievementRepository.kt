package com.viso.data.repository

import com.viso.data.db.dao.AchievementDao
import com.viso.data.db.entity.AchievementEntity
import com.viso.domain.model.Achievement
import com.viso.domain.model.AchievementType
import com.viso.domain.model.Rarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    fun getAllAchievements(): Flow<List<Achievement>> =
        achievementDao.getAllAchievements().map { list ->
            list.map { it.toDomain() }
        }

    fun getUnlockedAchievements(): Flow<List<Achievement>> =
        achievementDao.getUnlockedAchievements().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getById(id: String): Achievement? =
        achievementDao.getById(id)?.toDomain()

    suspend fun insert(achievement: Achievement) =
        achievementDao.insert(achievement.toEntity())

    suspend fun insertAll(achievements: List<Achievement>) =
        achievementDao.insertAll(achievements.map { it.toEntity() })

    suspend fun updateProgress(id: String, progress: Int) =
        achievementDao.updateProgress(id, progress)

    suspend fun unlockAchievement(id: String, timestamp: Long = System.currentTimeMillis()) =
        achievementDao.unlockAchievement(id, timestamp)

    suspend fun getUnlockedCount(): Int =
        achievementDao.getUnlockedCount()

    suspend fun initializeDefaultAchievements() {
        val defaultAchievements = listOf(
            // Streak achievements
            Achievement(
                id = "streak_3",
                type = AchievementType.STREAK,
                title = "Fogo Baixo",
                description = "3 meses pagando tudo em dia!",
                icon = "🔥",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 3,
                rarity = Rarity.COMMON
            ),
            Achievement(
                id = "streak_6",
                type = AchievementType.STREAK,
                title = "Fogo Médio",
                description = "6 meses pagando tudo em dia!",
                icon = "🔥🔥",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 6,
                rarity = Rarity.RARE
            ),
            Achievement(
                id = "streak_12",
                type = AchievementType.STREAK,
                title = "Fogo Alto",
                description = "1 ano pagando tudo em dia!",
                icon = "🔥🔥🔥",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 12,
                rarity = Rarity.EPIC
            ),
            Achievement(
                id = "streak_24",
                type = AchievementType.STREAK,
                title = "Mestre da Disciplina",
                description = "2 anos pagando tudo em dia!",
                icon = "👑",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 24,
                rarity = Rarity.LEGENDARY
            ),
            // Milestone achievements
            Achievement(
                id = "rule_master",
                type = AchievementType.MILESTONE,
                title = "Mestre do 70-20-10",
                description = "Manteve a regra por 3 meses seguidos",
                icon = "🎯",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 3,
                rarity = Rarity.RARE
            ),
            Achievement(
                id = "saving_3",
                type = AchievementType.SAVING,
                title = "Economizador",
                description = "Guardou 10% do salário por 3 meses",
                icon = "💰",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 3,
                rarity = Rarity.COMMON
            ),
            Achievement(
                id = "saving_6",
                type = AchievementType.SAVING,
                title = "Investidor",
                description = "Guardou 10% do salário por 6 meses",
                icon = "📈",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 6,
                rarity = Rarity.RARE
            ),
            Achievement(
                id = "emergency_fund_complete",
                type = AchievementType.MILESTONE,
                title = "Reserva Completa",
                description = "Atingiu sua meta de reserva de emergência",
                icon = "🏆",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 1,
                rarity = Rarity.EPIC
            ),
            Achievement(
                id = "first_bill",
                type = AchievementType.MILESTONE,
                title = "Primeiro Passo",
                description = "Cadastrou sua primeira conta",
                icon = "📝",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 1,
                rarity = Rarity.COMMON
            ),
            Achievement(
                id = "ten_bills",
                type = AchievementType.MILESTONE,
                title = "Organizador",
                description = "Cadastrou 10 contas fixas",
                icon = "📋",
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = 10,
                rarity = Rarity.RARE
            )
        )

        // Only insert if not exists
        defaultAchievements.forEach { achievement ->
            if (getById(achievement.id) == null) {
                insert(achievement)
            }
        }
    }

    private fun AchievementEntity.toDomain() = Achievement(
        id = id,
        type = AchievementType.valueOf(type),
        title = title,
        description = description,
        icon = icon,
        isUnlocked = unlockedAt != null,
        unlockedAt = unlockedAt,
        progress = progress,
        target = target,
        rarity = Rarity.valueOf(rarity)
    )

    private fun Achievement.toEntity() = AchievementEntity(
        id = id,
        type = type.name,
        title = title,
        description = description,
        icon = icon,
        unlockedAt = unlockedAt,
        progress = progress,
        target = target,
        rarity = rarity.name,
        createdAt = System.currentTimeMillis()
    )
}
