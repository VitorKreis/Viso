package com.viso.data.repository

import com.viso.domain.repository.MonthCloseSideEffects
import com.viso.domain.usecase.CheckAchievementsUseCase
import com.viso.domain.usecase.ScheduleNotificationsUseCase
import com.viso.domain.usecase.UpdateStreakUseCase
import javax.inject.Inject

class MonthCloseSideEffectsImpl @Inject constructor(
    private val scheduleNotif: ScheduleNotificationsUseCase,
    private val updateStreak: UpdateStreakUseCase,
    private val checkAchievements: CheckAchievementsUseCase
) : MonthCloseSideEffects {
    override suspend fun onAlreadyClosed() {
        checkAchievements()
        scheduleNotif()
    }

    override suspend fun onSuccessfulClose(monthCompleted: Boolean) {
        updateStreak(monthCompleted)
        checkAchievements()
        scheduleNotif()
    }
}
