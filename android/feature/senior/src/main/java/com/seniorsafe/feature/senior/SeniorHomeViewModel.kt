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
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeniorHomeUiState(
    val isServiceRunning: Boolean = false,
    val lastUnlockText: String = "아직 기록이 없어요",
    val todayUnlockCount: Int = 0,
    val uploadStatusText: String = "서버 연결 전이라 휴대폰 안에만 저장 중이에요"
)

@HiltViewModel
class SeniorHomeViewModel @Inject constructor(
    private val activityMonitorController: ActivityMonitorController,
    activityServiceStateStore: ActivityServiceStateStore,
    activityRepository: ActivityRepository,
    private val deviceDataStore: DeviceDataStore,
    todayMessageNotificationScheduler: TodayMessageNotificationScheduler
) : ViewModel() {

    val uiState: StateFlow<SeniorHomeUiState> =
        combine(
            activityServiceStateStore.isRunning,
            activityRepository.observeRecentUnlocks(100)
        ) { isRunning, unlocks ->
            SeniorHomeUiState(
                isServiceRunning = isRunning,
                lastUnlockText = unlocks.firstOrNull()?.toDisplayTime() ?: "아직 기록이 없어요",
                todayUnlockCount = unlocks.count { it.unlockedAtMillis >= startOfTodayMillis() }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeniorHomeUiState()
        )

    init {
        todayMessageNotificationScheduler.scheduleDailyEveningReminder()
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

private fun UnlockEventEntity.toDisplayTime(): String {
    val formatter = SimpleDateFormat("M월 d일 HH:mm", Locale.KOREA)
    return formatter.format(Date(unlockedAtMillis))
}

private fun startOfTodayMillis(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
