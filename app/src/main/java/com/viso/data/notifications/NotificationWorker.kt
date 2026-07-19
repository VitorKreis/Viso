package com.viso.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viso.R
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ConfigRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.YearMonth

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val billRepo: BillRepository,
    private val configRepo: ConfigRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val config = configRepo.getConfig()
        if (config.notifDaysBefore <= 0) return Result.success()

        val today = LocalDate.now()
        val targetDate = today.plusDays(config.notifDaysBefore.toLong())
        val targetMonth = YearMonth.now().toString()

        val bills = billRepo.getAllBills()
        val upcomingBills = bills.filter { bill ->
            !bill.isPaid &&
            bill.dueDay == targetDate.dayOfMonth &&
            bill.paidMonth != targetMonth
        }

        if (upcomingBills.isEmpty()) return Result.success()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lembretes de Contas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificações de contas a vencer"
        }
        notificationManager.createNotificationChannel(channel)

        upcomingBills.forEach { bill ->
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Conta a vencer em breve")
                .setContentText("${bill.name} - ${formatCurrency(bill.amountCents)}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(bill.id.hashCode(), notification)
        }

        return Result.success()
    }

    private fun formatCurrency(cents: Long): String {
        val reais = cents / 100
        val centavos = cents % 100
        return String.format("R$ %d,%02d", reais, centavos)
    }

    companion object {
        const val CHANNEL_ID = "viso_bills_reminder"
        const val WORK_NAME = "bill_notification_worker"
    }
}
