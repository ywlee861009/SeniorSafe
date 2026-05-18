package com.seniorsafe.core.diagnostics

import com.seniorsafe.core.util.KeroLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiagnosticLogEntry(
    val id: Long,
    val timestamp: String,
    val source: String,
    val message: String
)

@Singleton
class DiagnosticsLogStore @Inject constructor(
    private val dao: DiagnosticLogDao
) {

    companion object {
        private const val MAX_ENTRIES = 500
    }

    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val entries: StateFlow<List<DiagnosticLogEntry>> =
        dao.observeRecent(MAX_ENTRIES)
            .map { entities ->
                entities.map { entity ->
                    DiagnosticLogEntry(
                        id = entity.id,
                        timestamp = entity.timestamp,
                        source = entity.source,
                        message = entity.message
                    )
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    @Synchronized
    fun add(source: String, message: String) {
        val now = System.currentTimeMillis()
        val entry = DiagnosticLogEntity(
            timestampMillis = now,
            timestamp = formatter.format(Date(now)),
            source = source,
            message = message
        )
        scope.launch {
            dao.insert(entry)
            dao.pruneToLatest(MAX_ENTRIES)
        }
        KeroLog.d("[$source] $message")
    }

    @Synchronized
    fun clear() {
        scope.launch {
            dao.clear()
            val now = System.currentTimeMillis()
            dao.insert(
                DiagnosticLogEntity(
                    timestampMillis = now,
                    timestamp = formatter.format(Date(now)),
                    source = "Diagnostics",
                    message = "diagnostics database cleared"
                )
            )
        }
        KeroLog.d("[Diagnostics] diagnostics database cleared")
    }
}
