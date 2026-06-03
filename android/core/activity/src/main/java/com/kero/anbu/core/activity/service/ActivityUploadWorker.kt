package com.kero.anbu.core.activity.service

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kero.anbu.core.activity.db.ActivityRepository
import com.kero.anbu.core.diagnostics.DiagnosticsLogStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

class ActivityUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ActivityUploadWorkerEntryPoint::class.java
        )
        val diagnosticsLogStore = entryPoint.diagnosticsLogStore()
        return try {
            val uploadResult = entryPoint.activityRepository().uploadPendingEvents()
            diagnosticsLogStore.add(
                "ActivityUploadWorker",
                "uploaded pending events: activity=${uploadResult.activityUploaded}, service=${uploadResult.serviceUploaded}"
            )
            Result.success()
        } catch (e: Exception) {
            diagnosticsLogStore.add(
                "ActivityUploadWorker",
                "upload failed: ${e.javaClass.simpleName}: ${e.message.orEmpty()}"
            )
            Result.retry()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ActivityUploadWorkerEntryPoint {
        fun activityRepository(): ActivityRepository
        fun diagnosticsLogStore(): DiagnosticsLogStore
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "activity_event_upload_periodic"
        private const val IMMEDIATE_WORK_NAME = "activity_event_upload_immediate"
        private const val PERIODIC_INTERVAL_MINUTES = 15L

        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<ActivityUploadWorker>(
                PERIODIC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
                .setConstraints(networkConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun enqueueImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<ActivityUploadWorker>()
                .setConstraints(networkConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        private fun networkConstraints(): Constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
    }
}
