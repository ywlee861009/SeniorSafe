package com.seniorsafe.feature.senior.today

import java.util.Calendar
import javax.inject.Inject

class TodayMessageProvider @Inject constructor() {
    private val messages = listOf(
        "오늘도 천천히, 편안하게 보내세요.",
        "작은 움직임 하나가 좋은 하루를 만들어요.",
        "따뜻한 물 한 잔으로 잠시 쉬어가세요.",
        "지금의 평온함을 충분히 누리세요.",
        "괜찮은 하루가 차곡차곡 쌓이고 있어요.",
        "오늘 할 일은 서두르지 않아도 괜찮아요.",
        "가까운 사람에게 안부 한마디를 전해보세요."
    )

    fun messageForToday(): String {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return messages[dayOfYear % messages.size]
    }
}
