package com.viso.ui.streaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.AchievementRepository
import com.viso.domain.model.Achievement
import com.viso.domain.model.StreakInfo
import com.viso.domain.usecase.CalculateStreaksUseCase
import com.viso.domain.usecase.CheckAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreaksUiState(
    val streakInfo: StreakInfo? = null,
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StreaksViewModel @Inject constructor(
    private val calculateStreaks: CalculateStreaksUseCase,
    private val checkAchievements: CheckAchievementsUseCase,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreaksUiState())
    val uiState: StateFlow<StreaksUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Initialize achievements if needed
            checkAchievements()

            combine(
                calculateStreaks(),
                achievementRepository.getAllAchievements()
            ) { streakInfo, achievements ->
                val unlocked = achievements.count { it.isUnlocked }
                StreaksUiState(
                    streakInfo = streakInfo,
                    achievements = achievements,
                    unlockedCount = unlocked,
                    totalCount = achievements.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun refreshAchievements() {
        viewModelScope.launch {
            checkAchievements()
        }
    }
}
