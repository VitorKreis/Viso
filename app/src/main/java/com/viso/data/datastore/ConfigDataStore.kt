package com.viso.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.viso.domain.model.Config
import com.viso.domain.model.SalaryMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "viso_config")

object ConfigKeys {
    val SALARY_CENTS = longPreferencesKey("salary_cents")
    val PAYDAY = intPreferencesKey("payday")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val NOTIF_DAYS_BEFORE = intPreferencesKey("notif_days_before")
    val LAST_RESET_MONTH = stringPreferencesKey("last_reset_month")
    val SALARY_MODE = stringPreferencesKey("salary_mode")
    val SALARY1_CENTS = longPreferencesKey("salary1_cents")
    val PAYDAY1 = intPreferencesKey("payday1")
    val SALARY2_CENTS = longPreferencesKey("salary2_cents")
    val PAYDAY2 = intPreferencesKey("payday2")
    val CURRENT_STREAK = intPreferencesKey("current_streak")
    val MAX_STREAK = intPreferencesKey("max_streak")
}

@Singleton
class ConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val configFlow: Flow<Config> = context.dataStore.data.map { prefs ->
        Config(
            salaryCents = prefs[ConfigKeys.SALARY_CENTS] ?: 0L,
            payday = prefs[ConfigKeys.PAYDAY] ?: 5,
            onboardingDone = prefs[ConfigKeys.ONBOARDING_DONE] ?: false,
            notifDaysBefore = prefs[ConfigKeys.NOTIF_DAYS_BEFORE] ?: 3,
            lastResetMonth = prefs[ConfigKeys.LAST_RESET_MONTH] ?: "",
            salaryMode = try {
                SalaryMode.valueOf(prefs[ConfigKeys.SALARY_MODE] ?: "SINGLE")
            } catch (_: Exception) {
                SalaryMode.SINGLE
            },
            salary1Cents = prefs[ConfigKeys.SALARY1_CENTS] ?: 0L,
            payday1 = prefs[ConfigKeys.PAYDAY1] ?: 5,
            salary2Cents = prefs[ConfigKeys.SALARY2_CENTS] ?: 0L,
            payday2 = prefs[ConfigKeys.PAYDAY2] ?: 20,
            currentStreak = prefs[ConfigKeys.CURRENT_STREAK] ?: 0,
            maxStreak = prefs[ConfigKeys.MAX_STREAK] ?: 0
        )
    }

    suspend fun getConfig(): Config = configFlow.first()

    suspend fun updateSalary(cents: Long) {
        context.dataStore.edit { it[ConfigKeys.SALARY_CENTS] = cents }
    }

    suspend fun updatePayday(day: Int) {
        context.dataStore.edit { it[ConfigKeys.PAYDAY] = day }
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[ConfigKeys.ONBOARDING_DONE] = true }
    }

    suspend fun updateNotifDaysBefore(days: Int) {
        context.dataStore.edit { it[ConfigKeys.NOTIF_DAYS_BEFORE] = days }
    }

    suspend fun updateLastResetMonth(month: String) {
        context.dataStore.edit { it[ConfigKeys.LAST_RESET_MONTH] = month }
    }

    suspend fun updateSalaryMode(mode: SalaryMode) {
        context.dataStore.edit { it[ConfigKeys.SALARY_MODE] = mode.name }
    }

    suspend fun updateSalary1(cents: Long) {
        context.dataStore.edit { it[ConfigKeys.SALARY1_CENTS] = cents }
    }

    suspend fun updatePayday1(day: Int) {
        context.dataStore.edit { it[ConfigKeys.PAYDAY1] = day }
    }

    suspend fun updateSalary2(cents: Long) {
        context.dataStore.edit { it[ConfigKeys.SALARY2_CENTS] = cents }
    }

    suspend fun updatePayday2(day: Int) {
        context.dataStore.edit { it[ConfigKeys.PAYDAY2] = day }
    }

    suspend fun updateStreak(current: Int, max: Int) {
        context.dataStore.edit {
            it[ConfigKeys.CURRENT_STREAK] = current
            it[ConfigKeys.MAX_STREAK] = max
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
