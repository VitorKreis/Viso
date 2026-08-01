package com.viso.domain.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import com.viso.notification.BillAlarmReceiver
import com.viso.notification.MonthSetupReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
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

        cancelMonthSetupReminder(alarmManager)

        bills.forEach { bill ->
            val intent = Intent(context, BillAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, bill.id.hashCode(), intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }

        if (config.notifDaysBefore <= 0) return

        scheduleMonthSetupReminder(alarmManager, config.notifHour)

        bills.filter { !it.isPaid }.forEach { bill ->
            val dueMonth = billDueMonth(bill, YearMonth.from(now))
            val dueDay = clampDayToMonth(bill.dueDay, dueMonth.year, dueMonth.monthValue)
            val triggerDay = (dueDay - config.notifDaysBefore).coerceAtLeast(1)
            val triggerDate = dueMonth.atDay(
                clampDayToMonth(triggerDay, dueMonth.year, dueMonth.monthValue)
            )
            if (triggerDate.isBefore(now)) return@forEach

            val triggerMillis = triggerDate
                .atTime(config.notifHour.coerceIn(6, 22), 0)
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

    private fun cancelMonthSetupReminder(alarmManager: AlarmManager) {
        val pi = PendingIntent.getBroadcast(
            context,
            MONTH_SETUP_REQUEST_CODE,
            Intent(context, MonthSetupReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { alarmManager.cancel(it) }
    }

    private suspend fun scheduleMonthSetupReminder(alarmManager: AlarmManager, hour: Int) {
        val config = configRepo.getConfig()
        val currentMonth = YearMonth.now()
        val currentMonthText = currentMonth.toString()
        val isPrepared = config.monthSetupPreparedMonth == currentMonthText
        val targetMonth = if (isPrepared) currentMonth.plusMonths(1) else currentMonth
        var triggerDate = if (isPrepared) {
            targetMonth.atDay(1)
        } else {
            LocalDate.now()
        }
        val safeHour = hour.coerceIn(6, 22)
        var triggerMillis = triggerDate
            .atTime(safeHour, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (!isPrepared && triggerMillis <= System.currentTimeMillis()) {
            triggerDate = triggerDate.plusDays(1)
            triggerMillis = triggerDate
                .atTime(safeHour, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        val intent = Intent(context, MonthSetupReminderReceiver::class.java).apply {
            putExtra("month", targetMonth.toString())
        }
        val pi = PendingIntent.getBroadcast(
            context,
            MONTH_SETUP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
    }

    companion object {
        private const val MONTH_SETUP_REQUEST_CODE = -73020
    }
}
