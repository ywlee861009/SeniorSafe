package com.kero.anbu.feature.guardian

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun String?.toDisplayTimeOrNone(): String =
    this?.toDisplayTime() ?: "없음"

/**
 * ISO-8601 → "6월 4일 21:34" (기기 로컬 시간).
 * Android는 "...Z"로 업로드하지만 PostgREST는 "...+00:00"으로 돌려줄 수 있어
 * 오프셋 표기(Z/+00:00) 양쪽을 받는 OffsetDateTime.parse를 쓴다.
 */
internal fun String.toDisplayTime(): String =
    runCatching {
        OffsetDateTime.parse(this)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("M월 d일 HH:mm", Locale.KOREA))
    }.getOrDefault(this)

internal fun String.toKoreanAlertStatus(): String =
    when (this) {
        "sent" -> "발송됨"
        "failed" -> "발송 실패"
        "skipped" -> "발송 안 됨"
        "deduplicated" -> "중복 제외"
        else -> this
    }
