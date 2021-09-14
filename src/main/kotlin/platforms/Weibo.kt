@file:OptIn(ExperimentalSerializationApi::class)

package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.platforms.WeiboResponseJson.InfoResponseJson
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Suppress("UNUSED")
class Weibo : PlatformResolver() {
    override val platformIdentifier = setOf("Weibo", "微博", "microblog", "新浪微博")

    override suspend fun GroupTarget.checkForUpdate(
        platformTargetData: PlatformTargetData,
        lastUpdateTime: Instant
    ): UpdateInfo? {
        TODO("Not yet implemented")
    }

    override suspend fun resolveTarget(params: List<String>): PlatformTargetData? {
        if (params.isEmpty()) return null
        val uid = params[1]
        val infoResponse = (try {
            // fetching the first 10 dynamics for now
            Fuel.get("https://m.weibo.cn/api/container/getIndex?containerid=100505${uid}").awaitString()
        } catch (exception: Exception) {
            return null
        })
        val infoResponseJson: InfoResponseJson = jsonParser.decodeFromString(infoResponse)
        return buildPlatformTarget(infoResponseJson.data.userInfo.screen_name, listOf(uid))
    }
}

private class WeiboResponseJson {
    @Serializable
    data class InfoResponseJson(
        val data: InfoData
    ) {
        @Serializable
        data class InfoData(
            val userInfo: UserInfo
        ) {
            @Serializable
            data class UserInfo(
                val screen_name: String
            )
        }
    }
}
