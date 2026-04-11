package com.viso.domain.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.notification.BillAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ScheduleNotificationsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billRepo: BillRepository,
    private val configRepo: ConfigRepository
) {
    suspend operator fun invoke() {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val config = configRepo.getConfig()
        val bills = billRepo.getAllBills()
        val now = LocalDate.now()

        bills.forEach { bill ->
            val intent = Intent(context, BillAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, bill.id.hashCode(), intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }

        bills.filter { !it.isPaid }.forEach { bill ->
            val dueDay = clampDayToMonth(bill.dueDay, now.year, now.monthValue)
            val triggerDay = (dueDay - config.notifDaysBefore).coerceAtLeast(1)
            val triggerDate = now.withDayOfMonth(
                clampDayToMonth(triggerDay, now.year, now.monthValue)
            )
            if (triggerDate.isBefore(now)) return@forEach

            val triggerMillis = triggerDate
                .atTime(9, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val intent = Intent(context, BillAlarmReceiver::class.java).apply {
                putExtra("bill_id", bill.id)
                putExtra("bill_name", bill.name)
                putExtra("due_day", bill.dueDay)
                putExtra("amount_cents", bill.amountCents)
            }
            val pi = PendingIntent.getBroadcast(
                context, bill.id.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
        }
    }
}
