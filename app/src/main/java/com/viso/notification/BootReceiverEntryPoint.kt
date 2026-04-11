package com.viso.notification

import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootReceiverEntryPoint {
    fun billRepository(): BillRepository
    fun configRepository(): ConfigRepository
}
