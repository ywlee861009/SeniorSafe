package com.kero.anbu.core.activity.db

import com.kero.anbu.core.model.ActivityEventUploadItem
import com.kero.anbu.core.model.ActivityEventsUploadRequest
import com.kero.anbu.core.model.ServiceEventUploadItem
import com.kero.anbu.core.model.ServiceEventsUploadRequest
import com.kero.anbu.core.network.ApiService
import java.time.Instant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ActivityRepository @Inject constructor(
    private val unlockDao: UnlockEventDao,
    private val serviceDao: ServiceEventDao,
    private val api: ApiService
) {

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val uploadMutex = Mutex()

    suspend fun recordUnlock(
        timestampMillis: Long = System.currentTimeMillis(),
        source: String = "user_present"
    ) {
        unlockDao.insert(
            UnlockEventEntity(
                unlockedAtMillis = timestampMillis,
                unlockedAt = formatter.format(Date(timestampMillis)),
                source = source
            )
        )
    }

    suspend fun recordServiceEvent(eventType: String, detail: String = "") {
        val now = System.currentTimeMillis()
        serviceDao.insert(
            ServiceEventEntity(
                eventType = eventType,
                occurredAtMillis = now,
                occurredAt = formatter.format(Date(now)),
                detail = detail
            )
        )
    }

    fun observeRecentUnlocks(limit: Int = 100): Flow<List<UnlockEventEntity>> =
        unlockDao.observeRecent(limit)

    fun observeRecentServiceEvents(limit: Int = 100): Flow<List<ServiceEventEntity>> =
        serviceDao.observeRecent(limit)

    suspend fun unlockCount(): Int = unlockDao.count()
    suspend fun serviceEventCount(): Int = serviceDao.count()

    suspend fun uploadPendingEvents(limit: Int = DEFAULT_UPLOAD_BATCH_SIZE): UploadResult =
        uploadMutex.withLock {
            uploadPendingEventsLocked(limit)
        }

    private suspend fun uploadPendingEventsLocked(limit: Int): UploadResult {
        val activityUploaded = uploadPendingActivityEvents(limit)
        val serviceUploaded = uploadPendingServiceEvents(limit)
        return UploadResult(
            activityUploaded = activityUploaded,
            serviceUploaded = serviceUploaded
        )
    }

    private suspend fun uploadPendingActivityEvents(limit: Int): Int {
        val pending = unlockDao.getPendingUpload(limit)
        if (pending.isEmpty()) return 0

        val request = ActivityEventsUploadRequest(
            events = pending.map { event ->
                ActivityEventUploadItem(
                    occurredAt = event.unlockedAtMillis.toIsoString(),
                    source = event.source
                )
            }
        )
        val response = api.recordActivityEvents(request)
        if (response.accepted > 0) {
            unlockDao.markUploaded(pending.take(response.accepted).map { it.id })
        }
        return response.accepted
    }

    private suspend fun uploadPendingServiceEvents(limit: Int): Int {
        val pending = serviceDao.getPendingUpload(limit)
        if (pending.isEmpty()) return 0

        val request = ServiceEventsUploadRequest(
            events = pending.map { event ->
                ServiceEventUploadItem(
                    eventType = event.eventType,
                    occurredAt = event.occurredAtMillis.toIsoString(),
                    detail = event.detail.ifBlank { null }
                )
            }
        )
        val response = api.recordServiceEvents(request)
        if (response.accepted > 0) {
            serviceDao.markUploaded(pending.take(response.accepted).map { it.id })
        }
        return response.accepted
    }

    private fun Long.toIsoString(): String =
        Instant.ofEpochMilli(this).toString()

    data class UploadResult(
        val activityUploaded: Int,
        val serviceUploaded: Int
    )

    companion object {
        private const val DEFAULT_UPLOAD_BATCH_SIZE = 50
    }
}
