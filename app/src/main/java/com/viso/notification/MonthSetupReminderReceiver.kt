package com.viso.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.viso.domain.usecase.ScheduleNotificationsUseCase
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.YearMonth

class MonthSetupReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            BootReceiverEntryPoint::class.java
        )
        val month = intent.getStringExtra("month") ?: YearMonth.now().toString()

        CoroutineScope(Dispatchers.IO).launch {
            val config = entryPoint.configRepository().getConfig()
            if (config.monthSetupPreparedMonth != month) {
                NotificationHelper(appContext).showMonthSetupNotification(month)
            }

            ScheduleNotificationsUseCase(
                context = appContext,
                billRepo = entryPoint.billRepository(),
                configRepo = entryPoint.configRepository()
            )()
        }
    }
}
