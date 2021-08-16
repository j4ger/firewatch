package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.Watcher
import cn.j4ger.firewatch.WatcherPlatformTarget
import cn.j4ger.firewatch.utils.parseJSTimestamp
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

const val defaultName = "<...>"

@Serializable
data class Bilibili(private val targetId: String, override var targetName: String = defaultName) :
    WatcherPlatformTarget {
    override val platformIdentifier = "Bilibili"

    // fetching the first 10 dynamics for now
    override val updateRequestUrl =
        "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=$targetId"
    override val infoRequestUrl = "https://api.bilibili.com/x/space/acc/info?mid=$targetId"

    override suspend fun resolveLastUpdateTime(response: HttpResponse): LocalDateTime {
        val parsedResult = Json.decodeFromString<DynamicResponseJson>(response.readText())
        return if (parsedResult.data.cards.isEmpty()) {
            LocalDateTime.ofInstant(Instant.EPOCH, TimeZone.getDefault().toZoneId())
        } else {
            parseJSTimestamp(parsedResult.data.cards[0].desc.timestamp)
        }
    }

    override suspend fun genUpdateMessage(response: HttpResponse, lastUpdateTime: LocalDateTime): Message {
        val parsedResult = Json.decodeFromString<DynamicResponseJson>(response.readText())
        val targetUpdates = parsedResult.data.cards.filter {
            parseJSTimestamp(it.desc.timestamp) > lastUpdateTime
        }
        return buildMessageChain {
            targetUpdates.forEach { dynamicCardInfo ->
                +PlainText(
                    buildString {
                        appendLine("$platformIdentifier $targetName 发布更新")
                        appendLine("动态链接：https://t.bilibili.com/${dynamicCardInfo.desc.dynamic_id}")
                    })

                when (dynamicCardInfo.desc.type) {
                    1 -> {
                        val textCard: TextCard = Json.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("动态内容：")
                                appendLine(textCard.item.content)
                            }
                        )
                    }
                    2 -> {
                        val imageCard: ImageCard = Json.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("动态内容：")
                                appendLine(imageCard.item.description)
                            }
                        )
                        imageCard.item.pictures.forEach {
                            +Image(Watcher.uploadImage(it.img_src).imageId)
                        }
                    }
                    8 -> {
                        val videoCard: VideoCard = Json.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("视频标题：")
                                appendLine(videoCard.title)
                                appendLine("视频简介：")
                                appendLine(videoCard.desc)
                            }
                        )
                        +Image(Watcher.uploadImage(videoCard.pic).imageId)
                        +PlainText(
                            buildString {
                                appendLine("视频链接：")
                                appendLine(videoCard.short_link ?: videoCard.short_link_v2 ?: "<Unresolved Link>")
                            }
                        )
                    }
                    64 -> {
                        val articleCard: ArticleCard = Json.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("专栏标题：")
                                appendLine(articleCard.title)
                                appendLine("动态内容：")
                                appendLine(articleCard.summary)
                            }
                        )
                        articleCard.banner_url?.let {
                            +Image(Watcher.uploadImage(it).imageId)
                        }
                    }
                    else -> {
                        +PlainText("<Unresolved Content>")
                    }
                }
            }
        }
    }

    override suspend fun resolveTargetName(response: HttpResponse): String {
        if (targetName == defaultName) {
            val parsedResult = Json.decodeFromString<InfoResponseJson>(response.readText())
            targetName = parsedResult.data.name
        }
        return targetName
    }

    override suspend fun resolveTargetValidity(response: HttpResponse): Boolean {
        val parsedResult: InfoResponseJson = Json.decodeFromString(response.readText())
        return parsedResult.code == 0
    }


}

@Serializable
private data class InfoResponseJson(val code: Int, val data: UserInfo) {
    @Serializable
    data class UserInfo(val name: String)
}

@Serializable
private data class DynamicResponseJson(val data: DynamicInfo) {
    @Serializable
    data class DynamicInfo(val cards: List<DynamicCardInfo>) {
        @Serializable
        data class DynamicCardInfo(val desc: DynamicCardDescription, val card: String) {
            @Serializable
            data class DynamicCardDescription(
                val type: Int,
                val timestamp: Long,
                val dynamic_id: String,
            )
        }

    }
}

// type: 1
@Serializable
private data class TextCard(val item: TextItem) {
    @Serializable
    data class TextItem(val content: String)
}

// type: 2
@Serializable
private data class ImageCard(val item: ImageItem) {
    @Serializable
    data class ImageItem(val pictures: List<Picture>, val description: String) {
        @Serializable
        data class Picture(val img_src: String)
    }
}

// type: 8
@Serializable
private data class VideoCard(
    val desc: String,
    val dynamic: String,
    val short_link: String?,
    val short_link_v2: String?,
    val title: String,
    val pic: String
)

// type: 64
@Serializable
private data class ArticleCard(
    val title: String,
    val summary: String,
    val banner_url: String?,
)

