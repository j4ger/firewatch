package cn.j4ger.firewatch

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.Message
import java.time.LocalDateTime

@Serializable
abstract class WatcherPlatformTarget {
    abstract var targetName: String
    abstract val platformIdentifier: String
    abstract val updateRequestUrl: String
    abstract val infoRequestUrl: String
    abstract suspend fun resolveLastUpdateTime(response: HttpResponse): Instant
    abstract suspend fun genUpdateMessage(response: HttpResponse, lastUpdateTime: Instant): Message
    abstract suspend fun resolveTargetName(response: HttpResponse): String
    abstract suspend fun resolveTargetValidity(response: HttpResponse): Boolean
}