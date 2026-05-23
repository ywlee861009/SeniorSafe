package com.kero.anbu.core.activity.db

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayMessageSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: TodayMessageDao
) {
    suspend fun seedIfNeeded() {
        if (dao.count() > 0) return

        val json = context.assets.open("today_messages.json")
            .bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val entities = (0 until array.length()).map { i ->
            TodayMessageEntity(message = array.getString(i))
        }
        dao.insertAll(entities)
    }
}
