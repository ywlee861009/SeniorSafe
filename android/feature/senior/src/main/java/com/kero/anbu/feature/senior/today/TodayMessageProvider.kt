package com.kero.anbu.feature.senior.today

import com.kero.anbu.core.activity.db.TodayMessageDao
import com.kero.anbu.core.activity.db.TodayMessageSeeder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayMessageProvider @Inject constructor(
    private val dao: TodayMessageDao,
    private val seeder: TodayMessageSeeder
) {
    /**
     * 매 호출마다 shownCount가 가장 낮은 메시지를 선택하고 노출 카운트를 1 증가시킨다.
     */
    suspend fun messageForToday(): String {
        seeder.seedIfNeeded()

        val entity = dao.pickLeastShown()
        val message = entity?.message ?: "오늘도 좋은 하루 보내세요."

        if (entity != null) {
            dao.incrementShownCount(entity.id)
        }

        return message
    }
}
