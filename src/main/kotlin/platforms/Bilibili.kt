@file:OptIn(ExperimentalSerializationApi::class)

package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.Watcher
import cn.j4ger.firewatch.platforms.BilibiliResponseJson.*
import cn.j4ger.firewatch.utils.parseJSTimestamp
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain

@Suppress("UNUSED")
class Bilibili : PlatformResolver() {
    override val platformIdentifier = setOf("Bili", "Bilibili", "哔哩哔哩")

    override suspend fun resolveTarget(params: List<String>): PlatformTargetData? {
        if (params.isEmpty()) return null
        val targetId = params[1]
        val infoResponse = (try {
            // fetching the first 10 dynamics for now
            Fuel.get("https://api.bilibili.com/x/space/acc/info?mid=${targetId}").awaitString()
        } catch (exception: Exception) {
            return null
        })
        val infoResponseJson: InfoResponseJson = jsonParser.decodeFromString(infoResponse)
        return buildPlatformTarget(
            infoResponseJson.data.name,
            listOf(targetId)
        )
    }

    override suspend fun GroupTarget.checkForUpdate(
        platformTargetData: PlatformTargetData,
        lastUpdateTime: Instant
    ): UpdateInfo? {
        val dynamicResponse = try {
            Fuel.get("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=${platformTargetData.params[0]}").awaitString()
        } catch (exception: Exception) {
            return null
        }
        val dynamicResponseJson: DynamicResponseJson = jsonParser.decodeFromString(dynamicResponse)
        if (dynamicResponseJson.data.cards.isEmpty()) return null
        val newLastUpdateTime = parseJSTimestamp(dynamicResponseJson.data.cards[0].desc.timestamp)
        val targetUpdates = dynamicResponseJson.data.cards.filter {
            parseJSTimestamp(it.desc.timestamp) > lastUpdateTime
        }
        if (targetUpdates.isEmpty()) {
            return null
        }
        val message = buildMessageChain {
            targetUpdates.forEach { dynamicCardInfo ->
                +PlainText(
                    buildString {
                        appendLine("[${platformTargetData.platformIdentifier}] [${platformTargetData.name}]")
                        appendLine("https://t.bilibili.com/${dynamicCardInfo.desc.dynamic_id}")
                        appendLine("========")
                    })

                when (dynamicCardInfo.desc.type) {
                    1 -> {
                        val textCard: TextCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine(textCard.item.content)
                            }.trim()
                        )
                    }
                    2 -> {
                        val imageCard: ImageCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine(imageCard.item.description)
                            }
                        )
                        imageCard.item.pictures.forEach {
                            +uploadImage(it.img_src)
                        }
                    }
                    4 -> {
                        val textCard: TextCard2 = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine(textCard.item.content)
                            }.trim()
                        )
                    }
                    8 -> {
                        val videoCard: VideoCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine(videoCard.title)
                                appendLine("--------")
                                appendLine(videoCard.desc)
                                appendLine("--------")
                                appendLine(videoCard.short_link ?: videoCard.short_link_v2 ?: "<Unresolved Link>")
                            }
                        )
                        +uploadImage(videoCard.pic)
                    }
                    64 -> {
                        val articleCard: ArticleCard = Watcher.jsonParser.decodeFromString(dynamicCardInfo.card)
                        +PlainText(
                            buildString {
                                appendLine(articleCard.title)
                                appendLine("--------")
                                appendLine(articleCard.summary)
                            }
                        )
                        articleCard.banner_url?.let {
                            +uploadImage(it)
                        }
                    }
                    else -> {
                        +PlainText("<Unresolved Content>")
                    }
                }
            }
        }
        return UpdateInfo(newLastUpdateTime, message)
    }
}

private class BilibiliResponseJson {
    @Serializable
    data class InfoResponseJson( val data: UserInfo) {
        @Serializable
        data class UserInfo(val name: String)
    }

    @Serializable
    data class DynamicResponseJson(val data: DynamicInfo) {
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
    data class TextCard(val item: TextItem) {
        @Serializable
        data class TextItem(val content: String)
    }

    // type: 2
    @Serializable
    data class ImageCard(val item: ImageItem) {
        @Serializable
        data class ImageItem(val pictures: List<Picture>, val description: String) {
            @Serializable
            data class Picture(val img_src: String)
        }
    }

    // type: 4
    @Serializable
    data class TextCard2(val item: TextItem2) {
        @Serializable
        data class TextItem2(val content: String)
    }

    // type: 8
    @Serializable
    data class VideoCard(
        val desc: String,
        val dynamic: String,
        val short_link: String?,
        val short_link_v2: String?,
        val title: String,
        val pic: String
    )

    // type: 64
    @Serializable
    data class ArticleCard(
        val title: String,
        val summary: String,
        val banner_url: String?,
    )
}
