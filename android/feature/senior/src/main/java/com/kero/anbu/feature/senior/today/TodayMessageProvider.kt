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
    private var launchMessage: String? = null

    /**
     * 이번 앱 실행에서 보여줄 메시지를 반환한다.
     * shownCount가 가장 낮은 메시지를 우선 선택하고, 노출 카운트를 1 증가시킨다.
     * 앱을 새로 실행하면 다시 선택하고, 같은 실행 안에서는 같은 메시지를 재사용한다.
     */
    suspend fun messageForToday(): String {
        seeder.seedIfNeeded()

        launchMessage?.let { return it }

        val entity = dao.pickLeastShown()
        val message = entity?.message ?: "오늘도 좋은 하루 보내세요."

        if (entity != null) {
            dao.incrementShownCount(entity.id)
        }

        launchMessage = message

        return message
    }
}
