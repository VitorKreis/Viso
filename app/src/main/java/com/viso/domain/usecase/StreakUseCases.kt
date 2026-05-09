package com.viso.domain.usecase

import com.viso.data.datastore.ConfigDataStore
import com.viso.data.repository.AchievementRepository
import com.viso.data.repository.BillRepository
import com.viso.data.repository.GoalRepository
import com.viso.domain.model.Achievement
import com.viso.domain.model.StreakInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateStreaksUseCase @Inject constructor(
    private val billRepository: BillRepository,
    private val configDataStore: ConfigDataStore,
    private val achievementRepository: AchievementRepository
) {
    operator fun invoke(): Flow<StreakInfo> =
        combine(
            billRepository.getAllBillsFlow(),
            configDataStore.configFlow
        ) { bills, config ->
            val currentMonth = YearMonth.now()
            val lastResetMonth = config.lastResetMonth

            // Calculate if last month was completed (all bills paid)
            val lastMonthCompleted = if (lastResetMonth.isNotBlank() && lastResetMonth != currentMonth.toString()) {
                val lastMonthBills = bills.filter { it.paidMonth == lastResetMonth }
                lastMonthBills.isNotEmpty() && lastMonthBills.all { it.isPaid }
            } else {
                false
            }

            // Calculate current streak from config
            val currentStreak = config.currentStreak
            val maxStreak = config.maxStreak

            // Calculate this month's progress
            val currentMonthBills = bills.filter { 
                it.paidMonth.isBlank() || it.paidMonth == currentMonth.toString() 
            }
            val totalBills = currentMonthBills.size
            val paidBills = currentMonthBills.count { it.isPaid }
            val thisMonthProgress = if (totalBills > 0) paidBills.toFloat() / totalBills.toFloat() else 0f

            // Calculate days remaining in month
            val today = LocalDate.now()
            val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())
            val daysRemaining = ChronoUnit.DAYS.between(today, lastDayOfMonth).toInt()

            StreakInfo(
                currentStreak = currentStreak,
                maxStreak = maxStreak,
                lastMonthCompleted = lastMonthCompleted,
                thisMonthProgress = thisMonthProgress,
                daysRemaining = daysRemaining
            )
        }
}

class CheckAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val billRepository: BillRepository,
    private val goalRepository: GoalRepository,
    private val configDataStore: ConfigDataStore
) {
    suspend operator fun invoke() {
        // Initialize achievements if needed
        achievementRepository.initializeDefaultAchievements()

        val config = configDataStore.getConfig()
        val bills = billRepository.getAllBills()
        val goals = goalRepository.getAllGoals()

        // Check streak achievements
        val currentStreak = config.currentStreak
        updateStreakAchievements(currentStreak)

        // Check first bill achievement
        if (bills.isNotEmpty()) {
            achievementRepository.unlockAchievement("first_bill")
        }

        // Check 10 bills achievement
        if (bills.size >= 10) {
            val achievement = achievementRepository.getById("ten_bills")
            if (achievement != null && !achievement.isUnlocked) {
                achievementRepository.updateProgress("ten_bills", bills.size)
                if (bills.size >= achievement.target) {
                    achievementRepository.unlockAchievement("ten_bills")
                }
            }
        }

        // Check emergency fund achievement
        val emergencyFund = goals.find { it.isEmergencyFund }
        if (emergencyFund != null && emergencyFund.currentAmountCents >= emergencyFund.targetAmountCents) {
            achievementRepository.unlockAchievement("emergency_fund_complete")
        }
    }

    private suspend fun updateStreakAchievements(currentStreak: Int) {
        val streakAchievements = listOf("streak_3", "streak_6", "streak_12", "streak_24")

        streakAchievements.forEach { id ->
            val achievement = achievementRepository.getById(id)
            if (achievement != null && !achievement.isUnlocked) {
                achievementRepository.updateProgress(id, currentStreak)
                if (currentStreak >= achievement.target) {
                    achievementRepository.unlockAchievement(id)
                }
            }
        }
    }
}

class UpdateStreakUseCase @Inject constructor(
    private val configDataStore: ConfigDataStore,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(monthCompleted: Boolean) {
        val config = configDataStore.getConfig()

        if (monthCompleted) {
            // Increment streak
            val newStreak = config.currentStreak + 1
            val newMaxStreak = maxOf(config.maxStreak, newStreak)
            configDataStore.updateStreak(newStreak, newMaxStreak)

            // Check achievements
            updateStreakAchievements(newStreak)
        } else {
            // Reset streak if month was not completed
            configDataStore.updateStreak(0, config.maxStreak)
        }
    }

    private suspend fun updateStreakAchievements(currentStreak: Int) {
        val streakAchievements = listOf("streak_3", "streak_6", "streak_12", "streak_24")

        streakAchievements.forEach { id ->
            val achievement = achievementRepository.getById(id)
            if (achievement != null && !achievement.isUnlocked) {
                achievementRepository.updateProgress(id, currentStreak)
                if (currentStreak >= achievement.target) {
                    achievementRepository.unlockAchievement(id)
                }
            }
        }
    }
}
