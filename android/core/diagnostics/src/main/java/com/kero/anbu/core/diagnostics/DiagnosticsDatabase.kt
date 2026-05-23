package com.kero.anbu.core.diagnostics

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "diagnostic_logs")
data class DiagnosticLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val timestamp: String,
    val source: String,
    val message: String
)

@Dao
interface DiagnosticLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiagnosticLogEntity)

    @Query(
        """
        SELECT * FROM (
            SELECT * FROM diagnostic_logs
            ORDER BY id DESC
            LIMIT :limit
        )
        ORDER BY id ASC
        """
    )
    fun observeRecent(limit: Int): Flow<List<DiagnosticLogEntity>>

    @Query(
        """
        DELETE FROM diagnostic_logs
        WHERE id NOT IN (
            SELECT id FROM diagnostic_logs
            ORDER BY id DESC
            LIMIT :limit
        )
        """
    )
    suspend fun pruneToLatest(limit: Int)

    @Query("DELETE FROM diagnostic_logs")
    suspend fun clear()
}

@Database(
    entities = [DiagnosticLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DiagnosticsDatabase : RoomDatabase() {
    abstract fun diagnosticLogDao(): DiagnosticLogDao
}
