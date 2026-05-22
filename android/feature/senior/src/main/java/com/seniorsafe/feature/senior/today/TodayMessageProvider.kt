package com.seniorsafe.feature.senior.today

import android.content.Context
import com.seniorsafe.core.activity.db.TodayMessageDao
import com.seniorsafe.core.activity.db.TodayMessageSeeder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayMessageProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: TodayMessageDao,
    private val seeder: TodayMessageSeeder
) {
    /**
     * 오늘의 메시지를 반환한다.
     * shownCount가 가장 낮은 메시지를 우선 선택하고, 노출 카운트를 1 증가시킨다.
     * 하루에 여러 번 호출되더라도 같은 날은 같은 메시지를 보여주기 위해
     * SharedPreferences에 당일 메시지를 캐시한다.
     */
    suspend fun messageForToday(): String {
        seeder.seedIfNeeded()

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = java.time.LocalDate.now().toString()
        val cachedDate = prefs.getString(KEY_DATE, null)
        val cachedMessage = prefs.getString(KEY_MESSAGE, null)

        if (cachedDate == today && cachedMessage != null) {
            return cachedMessage
        }

        val entity = dao.pickLeastShown()
        val message = entity?.message ?: "오늘도 좋은 하루 보내세요."

        if (entity != null) {
            dao.incrementShownCount(entity.id)
        }

        prefs.edit()
            .putString(KEY_DATE, today)
            .putString(KEY_MESSAGE, message)
            .apply()

        return message
    }

    companion object {
        private const val PREFS_NAME = "today_message_cache"
        private const val KEY_DATE = "cached_date"
        private const val KEY_MESSAGE = "cached_message"
    }
}
