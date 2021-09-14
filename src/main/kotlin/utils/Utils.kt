package cn.j4ger.firewatch.utils

import kotlinx.datetime.Instant


fun parseJSTimestamp(JSTimestamp: Long): Instant =
    Instant.fromEpochSeconds(JSTimestamp)
