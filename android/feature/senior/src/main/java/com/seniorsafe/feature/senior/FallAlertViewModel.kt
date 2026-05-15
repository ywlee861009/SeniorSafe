package com.seniorsafe.feature.senior

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.data.repository.FallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FallAlertViewModel @Inject constructor(
    private val fallRepository: FallRepository
) : ViewModel() {

    private var eventId: String? = null

    fun reportFall() {
        viewModelScope.launch {
            try {
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                val res = fallRepository.reportFall(now)
                eventId = res.eventId
            } catch (_: Exception) {}
        }
    }

    fun cancelFall() {
        viewModelScope.launch {
            try {
                eventId?.let { fallRepository.cancelFall(it) }
            } catch (_: Exception) {}
        }
    }
}
