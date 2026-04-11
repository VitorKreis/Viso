package com.viso.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.domain.usecase.ScheduleNotificationsUseCase
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                BootReceiverEntryPoint::class.java
            )
            val scheduleUseCase = ScheduleNotificationsUseCase(
                context = context.applicationContext,
                billRepo = entryPoint.billRepository(),
                configRepo = entryPoint.configRepository()
            )
            CoroutineScope(Dispatchers.IO).launch {
                scheduleUseCase()
            }
        }
    }
}
