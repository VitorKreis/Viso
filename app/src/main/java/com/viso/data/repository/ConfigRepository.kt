package com.viso.data.repository

import com.viso.data.datastore.ConfigDataStore
import com.viso.domain.model.Config
import com.viso.domain.model.SalaryMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val configDataStore: ConfigDataStore
) {
    val configFlow: Flow<Config> = configDataStore.configFlow

    suspend fun getConfig(): Config = configDataStore.getConfig()

    suspend fun updateSalary(cents: Long) = configDataStore.updateSalary(cents)

    suspend fun updatePayday(day: Int) = configDataStore.updatePayday(day)

    suspend fun setOnboardingDone() = configDataStore.setOnboardingDone()

    suspend fun updateNotifDaysBefore(days: Int) = configDataStore.updateNotifDaysBefore(days)

    suspend fun updateLastResetMonth(month: String) = configDataStore.updateLastResetMonth(month)

    suspend fun updateSalaryMode(mode: SalaryMode) = configDataStore.updateSalaryMode(mode)

    suspend fun updateSalary1(cents: Long) = configDataStore.updateSalary1(cents)

    suspend fun updatePayday1(day: Int) = configDataStore.updatePayday1(day)

    suspend fun updateSalary2(cents: Long) = configDataStore.updateSalary2(cents)

    suspend fun updatePayday2(day: Int) = configDataStore.updatePayday2(day)

    suspend fun clearAll() = configDataStore.clearAll()
}
