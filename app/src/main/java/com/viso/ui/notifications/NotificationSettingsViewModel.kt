package com.viso.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viso.data.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val enabled: Boolean = true,
    val daysBefore: Int = 3,
    val hour: Int = 9
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val configRepo: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = configRepo.getConfig()
            _uiState.value = NotificationSettingsUiState(
                enabled = config.notifDaysBefore > 0,
                daysBefore = config.notifDaysBefore.coerceAtLeast(1),
                hour = config.notifHour.coerceIn(6, 22)
            )
        }
    }

    fun onEnabledChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enabled = enabled)
    }

    fun onDaysBeforeChange(days: Int) {
        _uiState.value = _uiState.value.copy(daysBefore = days)
    }

    fun onHourChange(hour: Int) {
        _uiState.value = _uiState.value.copy(hour = hour)
    }

    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            configRepo.updateNotifDaysBefore(if (state.enabled) state.daysBefore else 0)
            configRepo.updateNotifHour(state.hour)
        }
    }
}
