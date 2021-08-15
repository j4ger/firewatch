package cn.j4ger.firewatch

import io.ktor.client.statement.*
import io.ktor.http.*
import net.mamoe.mirai.message.data.Message
import java.time.LocalDateTime

interface WatcherPlatformTarget {
    val platformIdentifier: String
    val updateRequestUrl: String
    val infoRequestUrl: String
    suspend fun resolveLastUpdateTime(response: HttpResponse): LocalDateTime
    suspend fun genUpdateMessage(response: HttpResponse, lastUpdateTime: LocalDateTime): Message
    suspend fun resolveTargetName(response: HttpResponse): String
    suspend fun resolveTargetValidity(response: HttpResponse): Boolean
}