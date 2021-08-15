package cn.j4ger.firewatch.utils

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

fun parseJSTimestamp(JSTimestamp: Long): LocalDateTime =
    LocalDateTime.ofInstant(
        Instant.ofEpochMilli(JSTimestamp),
        TimeZone.getDefault().toZoneId()
    )
