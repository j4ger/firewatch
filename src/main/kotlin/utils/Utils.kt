package cn.j4ger.firewatch.utils

import khttp.get
import khttp.responses.Response
import kotlinx.datetime.Instant


fun parseJSTimestamp(JSTimestamp: Long): Instant =
    Instant.fromEpochSeconds(JSTimestamp)

fun getWithUA(urlString: String): Response =
    get(
        urlString,
        headers = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.58 Safari/537.36")
    )
