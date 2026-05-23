package com.kero.anbu.core.diagnostics

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DiagnosticsEntryPoint {
    fun diagnosticsLogStore(): DiagnosticsLogStore
}

fun Context.diagnosticsLogStore(): DiagnosticsLogStore =
    EntryPointAccessors.fromApplication(
        applicationContext,
        DiagnosticsEntryPoint::class.java
    ).diagnosticsLogStore()
