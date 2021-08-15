package cn.j4ger.firewatch

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.Closeable

//TODO: error handling

object Watcher : Closeable {
    private val bot = Bot.instances[0]
    private val botSelfContact = bot.asFriend as Contact

    private lateinit var job: Job
    lateinit var httpClient: HttpClient

    private val watcherScope = CoroutineScope(Dispatchers.IO)
    suspend fun init() {
        httpClient = HttpClient(CIO) {
            install(JsonFeature)
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

    suspend fun checkForUpdate(target: WatcherPlatformTarget, contactId: Set<Long>) {
        val response: HttpResponse = httpClient.get(target.updateRequestUrl)
        val lastUpdateTime = target.resolveLastUpdateTime(response)
        val localLastUpdateTime = FirewatchData.lastUpdateTime[target] ?: lastUpdateTime
        if (lastUpdateTime > FirewatchData.lastUpdateTime[target]) {
            val message = target.genUpdateMessage(response, localLastUpdateTime)
            contactId.forEach { id ->
                bot.getGroup(id)?.let {
                    it.sendMessage(message)
                }
            }
        }
        FirewatchData.lastUpdateTime[target] = lastUpdateTime
    }

    suspend fun uploadImage(sourceUrl: String): Image {
        val filename = sourceUrl.substring(sourceUrl.lastIndexOf("/"))
        val imageResponse: HttpResponse = httpClient.get(sourceUrl)
        val imageBytes: ByteArray = imageResponse.receive()
        val externalResource = imageBytes.toExternalResource(filename)
        return botSelfContact.uploadImage(externalResource)
    }

    suspend fun respawn() {
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