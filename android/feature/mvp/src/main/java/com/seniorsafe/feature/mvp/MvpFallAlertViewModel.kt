package com.seniorsafe.feature.mvp

import androidx.lifecycle.ViewModel
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MvpFallAlertViewModel @Inject constructor(
    private val diagnosticsLogStore: DiagnosticsLogStore
) : ViewModel() {

    fun log(message: String) {
        diagnosticsLogStore.add("MvpFallAlert", message)
    }
}
