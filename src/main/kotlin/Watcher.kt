package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.PlatformResolverProvider
import cn.j4ger.firewatch.platforms.PlatformTargetData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
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
        jsonParser = Json {
            ignoreUnknownKeys = true
        }
        httpClient = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(jsonParser)
            }
        }
        //TODO: test target validity on init (one-time operation, probably by lazy)
        job = watcherScope.launch {
            while (true) {
                coroutineScope {
                    FirewatchData.targets.forEach {
                        println("Spawning task")
                        launch {
                            runCatching {
                                checkForUpdate(it.key, it.value)
                            }.onFailure {
                                Firewatch.logger.info(it.message)
                            }
                            Firewatch.logger.info("Task finished")
                        }
                    }
                }
                println("Delaying")
                delay(FirewatchData.updateInterval)
            }
        }
    }

    private suspend fun checkForUpdate(target: PlatformTargetData, contactId: Set<Long>) {
        val localLastUpdateTime = FirewatchData.lastUpdateTime[target] ?: Instant.DISTANT_PAST
        val resolver = PlatformResolverProvider.resolvePlatformTarget(target.platformIdentifier)
        resolver?.checkForUpdate(target, localLastUpdateTime)?.let {
            contactId.forEach { id ->
                println("Sending to $id")
                Bot.instances[0].getGroup(id)?.sendMessage(it.message)
            }
            println("Updating")
            FirewatchData.setLastUpdateTime(target, it.lastUpdateTime)
        }
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