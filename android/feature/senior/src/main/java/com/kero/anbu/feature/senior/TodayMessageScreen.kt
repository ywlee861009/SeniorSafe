package com.kero.anbu.feature.senior

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.activity.db.ActivityRepository
import com.kero.anbu.core.ui.theme.Neutral100
import com.kero.anbu.core.ui.theme.Neutral600
import com.kero.anbu.core.ui.theme.Neutral900
import com.kero.anbu.feature.senior.today.TodayMessageProvider
import com.kero.anbu.feature.senior.today.TodayMessageNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun TodayMessageScreen(
    onBack: () -> Unit,
    viewModel: TodayMessageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "오늘의 글",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = uiState.dateText,
            fontSize = 18.sp,
            color = Neutral600
        )
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Neutral100
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.message,
                    fontSize = 26.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Neutral900
                )
                Text(
                    text = "내일도 새로운 글이 도착해요.",
                    fontSize = 18.sp,
                    color = Neutral600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class TodayMessageUiState(
    val dateText: String = "",
    val message: String = ""
)

@HiltViewModel
class TodayMessageViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val todayMessageProvider: TodayMessageProvider,
    todayMessageNotificationScheduler: TodayMessageNotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TodayMessageUiState(
            dateText = SimpleDateFormat("M월 d일", Locale.KOREA).format(Date())
        )
    )
    val uiState: StateFlow<TodayMessageUiState> = _uiState

    init {
        todayMessageNotificationScheduler.scheduleDailyEveningReminder()
        viewModelScope.launch {
            val message = todayMessageProvider.messageForToday()
            _uiState.value = _uiState.value.copy(message = message)
            activityRepository.recordServiceEvent(
                eventType = "today_message_opened",
                detail = _uiState.value.dateText
            )
        }
    }
}
