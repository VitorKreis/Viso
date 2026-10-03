package com.viso.data.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viso.domain.usecase.ScheduleNotificationsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BootRescheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scheduleNotifications: ScheduleNotificationsUseCase
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        scheduleNotifications()
    }.fold(
        onSuccess = { Result.success() },
        onFailure = { Result.retry() }
    )

    companion object {
        const val WORK_NAME = "boot_reschedule_notifications"
    }
}
