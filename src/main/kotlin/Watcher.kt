package cn.j4ger.firewatch

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.Closeable

//TODO: error handling

object Watcher : Closeable {
    private lateinit var job: Job
    lateinit var httpClient: HttpClient
    lateinit var jsonParser: Json

    private val watcherScope = CoroutineScope(Dispatchers.IO)
    fun init() {
        jsonParser = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        }
        httpClient = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(jsonParser)
            }
        }
        job = watcherScope.launch {
            FirewatchConfig.targets.forEach {
                launch {
                    while (true) {
                        runCatching {
                            checkForUpdate(it.key, it.value)
                        }.onFailure {
                            Firewatch.logger.info(it)
                        }
                        delay(FirewatchConfig.updateInterval)
                    }
                }

            }
        }
    }

    private suspend fun checkForUpdate(target: WatcherPlatformTarget, contactId: Set<Long>) {
        val response: HttpResponse = httpClient.get(target.updateRequestUrl)
        val lastUpdateTime = target.resolveLastUpdateTime(response)
        val localLastUpdateTime = FirewatchData.lastUpdateTime[target] ?: lastUpdateTime
        if (lastUpdateTime > localLastUpdateTime) {
            val message = target.genUpdateMessage(response, localLastUpdateTime)
            contactId.forEach { id ->
                Bot.instances[0].getGroup(id)?.sendMessage(message)
            }
        }
        FirewatchData.lastUpdateTime[target] = lastUpdateTime
    }

    suspend fun uploadImage(sourceUrl: String): Image {
        val filename = sourceUrl.substring(sourceUrl.lastIndexOf("/"))
        val imageResponse: HttpResponse = httpClient.get(sourceUrl)
        val imageBytes: ByteArray = imageResponse.receive()
        val externalResource = imageBytes.toExternalResource(filename)
        return (Bot.instances[0].asFriend as Contact).uploadImage(externalResource)
    }

    fun respawn() {
        close()
        init()
    }

    override fun close() {
        if (::job.isInitialized) {
            job.cancel()
        }
        if (::httpClient.isInitialized) {
            httpClient.close()
        }
    }

}