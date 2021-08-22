package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.Firewatch
import cn.j4ger.firewatch.Watcher
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.Instant
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource

data class PlatformTargetData(
    val platformIdentifier: String,
    val name: String,
    val params: MutableList<String> = mutableListOf()
) {
    companion object {
        fun deserialize(source: String): PlatformTargetData {
            source.split(":-:").let {
                val paramList = it[2].drop(1).dropLast(1).split(",").toMutableList()
                return@deserialize PlatformTargetData(it[0], it[1], paramList)
            }
        }
    }

    fun serialize(): String {
        return "${this.platformIdentifier}:-:${this.name}:-:${this.params}"
    }

}

data class UpdateInfo(
    val lastUpdateTime: Instant,
    val message: Message
)

abstract class PlatformResolver {
    abstract val platformIdentifier: Set<String>
    abstract suspend fun resolveTarget(params: List<String>): PlatformTargetData?
    abstract suspend fun checkForUpdate(platformTargetData: PlatformTargetData, lastUpdateTime: Instant): UpdateInfo?

    suspend fun uploadImage(sourceUrl: String): Image {
        val filename = sourceUrl.substring(sourceUrl.lastIndexOf("/"))
        val imageResponse: HttpResponse = Watcher.httpClient.get(sourceUrl)
        val imageBytes: ByteArray = imageResponse.receive()
        val externalResource = imageBytes.toExternalResource(filename)
        return (Bot.instances[0].asFriend as Contact).uploadImage(externalResource)
    }

    val httpClient = Watcher.httpClient
    val jsonParser = Watcher.jsonParser
}