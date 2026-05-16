package com.seniorsafe.core.activity.db

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ActivityRepository @Inject constructor(
    private val unlockDao: UnlockEventDao,
    private val serviceDao: ServiceEventDao
) {

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

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
}
