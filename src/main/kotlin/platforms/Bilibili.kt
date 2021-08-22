package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.Firewatch
import cn.j4ger.firewatch.Watcher
import cn.j4ger.firewatch.utils.parseJSTimestamp
import io.ktor.client.request.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain

class Bilibili : PlatformResolver() {
    override val platformIdentifier = setOf("Bili", "Bilibili", "哔哩哔哩")
    override suspend fun resolveTarget(params: List<String>): PlatformTargetData? {
        if (params.isEmpty()) return null
        val targetId = params[1]
        val infoResponseJson = (try {
            // fetching the first 10 dynamics for now
            httpClient.get<InfoResponseJson>("https://api.bilibili.com/x/space/acc/info?mid=${targetId}")
        } catch (exception: Exception) {
            return null
        })
        return PlatformTargetData(
            platformIdentifier.first(),
            infoResponseJson.data.name,
            mutableListOf(targetId)
        )
    }

    override suspend fun checkForUpdate(
        platformTargetData: PlatformTargetData,
        lastUpdateTime: Instant
    ): UpdateInfo? {
        val dynamicResponseJson = try {
            httpClient.get<DynamicResponseJson>("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=${platformTargetData.params[0]}")
        } catch (exception: Exception) {
            Firewatch.logger.info(exception)
            return null
        }
        if (dynamicResponseJson.data.cards.isEmpty()) return null
        val newLastUpdateTime = parseJSTimestamp(dynamicResponseJson.data.cards[0].desc.timestamp)
        val targetUpdates = dynamicResponseJson.data.cards.filter {
            parseJSTimestamp(it.desc.timestamp) > lastUpdateTime
        }
        println("Got ${targetUpdates.size} update(s)")
        if (targetUpdates.isEmpty()) {
            return null
        }
        val message = buildMessageChain {
            targetUpdates.forEach { dynamicCardInfo ->
                +PlainText(
                    buildString {
                        appendLine("${platformTargetData.platformIdentifier} ${platformTargetData.name} 发布更新")
                        appendLine("动态链接：https://t.bilibili.com/${dynamicCardInfo.desc.dynamic_id}")
                    }.trim())

                when (dynamicCardInfo.desc.type) {
                    1 -> {
                        val textCard: TextCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("动态内容：")
                                appendLine(textCard.item.content)
                            }.trim()
                        )
                    }
                    2 -> {
                        val imageCard: ImageCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("动态内容：")
                                appendLine(imageCard.item.description)
                            }.trim()
                        )
                        imageCard.item.pictures.forEach {
                            +Image(Watcher.uploadImage(it.img_src).imageId)
                        }
                    }
                    4 -> {
                        val textCard: TextCard2 = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("动态内容：")
                                appendLine(textCard.item.content)
                            }.trim()
                        )
                    }
                    8 -> {
                        val videoCard: VideoCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("视频标题：")
                                appendLine(videoCard.title)
                                appendLine("视频简介：")
                                appendLine(videoCard.desc)
                                appendLine("视频链接：")
                                appendLine(videoCard.short_link ?: videoCard.short_link_v2 ?: "<Unresolved Link>")
                            }.trim()
                        )
                        +Image(Watcher.uploadImage(videoCard.pic).imageId)
                    }
                    64 -> {
                        val articleCard: ArticleCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine("专栏标题：")
                                appendLine(articleCard.title)
                                appendLine("动态内容：")
                                appendLine(articleCard.summary)
                            }.trim()
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
        println("Built message:$message")
        return UpdateInfo(newLastUpdateTime, message)
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
                val dynamic_id: Long,
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

// type: 4
@Serializable
private data class TextCard2(val item: TextItem2) {
    @Serializable
    data class TextItem2(val content: String)
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

