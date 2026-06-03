package com.kero.anbu.feature.guardian

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun String?.toDisplayTimeOrNone(): String =
    this?.toDisplayTime() ?: "없음"

internal fun String.toDisplayTime(): String =
    runCatching {
        DateTimeFormatter
            .ofPattern("M월 d일 HH:mm", Locale.KOREA)
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(this))
    }.getOrDefault(this)

internal fun String.toKoreanAlertStatus(): String =
    when (this) {
        "sent" -> "발송됨"
        "failed" -> "발송 실패"
        "skipped" -> "발송 안 됨"
        "deduplicated" -> "중복 제외"
        else -> this
    }
