package com.seniorsafe.core.activity.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "today_messages")
data class TodayMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val message: String,
    val shownCount: Int = 0
)

@Dao
interface TodayMessageDao {

    @Query("SELECT COUNT(*) FROM today_messages")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<TodayMessageEntity>)

    @Query("SELECT * FROM today_messages ORDER BY shownCount ASC, RANDOM() LIMIT 1")
    suspend fun pickLeastShown(): TodayMessageEntity?

    @Query("UPDATE today_messages SET shownCount = shownCount + 1 WHERE id = :id")
    suspend fun incrementShownCount(id: Long)
}

@Database(
    entities = [TodayMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TodayMessageDatabase : RoomDatabase() {
    abstract fun todayMessageDao(): TodayMessageDao
}
