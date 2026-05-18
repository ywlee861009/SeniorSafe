package com.seniorsafe.feature.senior

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.activity.db.ActivityRepository
import com.seniorsafe.core.activity.db.UnlockEventEntity
import com.seniorsafe.core.activity.service.ActivityMonitorController
import com.seniorsafe.core.activity.service.ActivityServiceStateStore
import com.seniorsafe.core.datastore.DeviceDataStore
import com.seniorsafe.core.model.PairingStatus
import com.seniorsafe.feature.senior.today.TodayMessageNotificationScheduler
import com.seniorsafe.feature.senior.today.TodayMessageProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeniorHomeUiState(
    val isServiceRunning: Boolean = false,
    val message: String = "",
    val recentEvents: List<UnlockEventEntity> = emptyList()
)

@HiltViewModel
class SeniorHomeViewModel @Inject constructor(
    private val activityMonitorController: ActivityMonitorController,
    activityServiceStateStore: ActivityServiceStateStore,
    activityRepository: ActivityRepository,
    private val deviceDataStore: DeviceDataStore,
    todayMessageNotificationScheduler: TodayMessageNotificationScheduler,
    todayMessageProvider: TodayMessageProvider
) : ViewModel() {

    private val todayMessage = todayMessageProvider.messageForToday()

    val uiState: StateFlow<SeniorHomeUiState> =
        combine(
            activityServiceStateStore.isRunning,
            activityRepository.observeRecentUnlocks(50)
        ) { isRunning, events ->
            SeniorHomeUiState(
                isServiceRunning = isRunning,
                message = todayMessage,
                recentEvents = events
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeniorHomeUiState(message = todayMessage)
        )

    init {
        todayMessageNotificationScheduler.scheduleDailyEveningReminder()
        activityMonitorController.ensureServiceRunning("senior home opened")
    }

    fun startMonitoring() {
        activityMonitorController.startByUser("senior home start button")
    }

    fun stopMonitoring() {
        activityMonitorController.stopByUser("senior home stop button")
    }

    fun resetPairingForDebug(onComplete: () -> Unit) {
        viewModelScope.launch {
            deviceDataStore.savePairingStatus(PairingStatus.UNPAIRED)
            onComplete()
        }
    }
}
