package com.seniorsafe.core.activity.di

import android.content.Context
import androidx.room.Room
import com.seniorsafe.core.activity.db.ActivityDatabase
import com.seniorsafe.core.activity.db.ServiceEventDao
import com.seniorsafe.core.activity.db.UnlockEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ActivityModule {

    @Provides
    @Singleton
    fun provideActivityDatabase(
        @ApplicationContext context: Context
    ): ActivityDatabase =
        Room.databaseBuilder(
            context,
            ActivityDatabase::class.java,
            "seniorsafe_activity.db"
        ).addMigrations(ActivityDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideUnlockEventDao(database: ActivityDatabase): UnlockEventDao =
        database.unlockEventDao()

    @Provides
    fun provideServiceEventDao(database: ActivityDatabase): ServiceEventDao =
        database.serviceEventDao()
}
