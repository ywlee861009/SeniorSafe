package com.seniorsafe.core.activity.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "unlock_events")
data class UnlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unlockedAtMillis: Long,
    val unlockedAt: String,
    val uploaded: Boolean = false
)

@Entity(tableName = "service_events")
data class ServiceEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val occurredAtMillis: Long,
    val occurredAt: String,
    val detail: String = "",
    val uploaded: Boolean = false
)

@Dao
interface UnlockEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UnlockEventEntity)

    @Query("SELECT * FROM unlock_events ORDER BY id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<UnlockEventEntity>>

    @Query("SELECT COUNT(*) FROM unlock_events")
    suspend fun count(): Int

    @Query("SELECT * FROM unlock_events WHERE uploaded = 0 ORDER BY id ASC LIMIT :limit")
    suspend fun getPendingUpload(limit: Int): List<UnlockEventEntity>

    @Query("UPDATE unlock_events SET uploaded = 1 WHERE id IN (:ids)")
    suspend fun markUploaded(ids: List<Long>)
}

@Dao
interface ServiceEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ServiceEventEntity)

    @Query("SELECT * FROM service_events ORDER BY id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ServiceEventEntity>>

    @Query("SELECT COUNT(*) FROM service_events")
    suspend fun count(): Int
}

@Database(
    entities = [UnlockEventEntity::class, ServiceEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ActivityDatabase : RoomDatabase() {
    abstract fun unlockEventDao(): UnlockEventDao
    abstract fun serviceEventDao(): ServiceEventDao
}
