package com.seniorsafe.core.diagnostics

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiagnosticsModule {

    @Provides
    @Singleton
    fun provideDiagnosticsDatabase(
        @ApplicationContext context: Context
    ): DiagnosticsDatabase =
        Room.databaseBuilder(
            context,
            DiagnosticsDatabase::class.java,
            "seniorsafe_diagnostics.db"
        ).build()

    @Provides
    fun provideDiagnosticLogDao(database: DiagnosticsDatabase): DiagnosticLogDao =
        database.diagnosticLogDao()
}
