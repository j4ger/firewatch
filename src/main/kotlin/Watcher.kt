package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.PlatformResolverProvider
import cn.j4ger.firewatch.platforms.PlatformTargetData
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import java.io.Closeable

//TODO: error handling

object Watcher : Closeable {
    private lateinit var job: Job
    lateinit var jsonParser: Json

    private val watcherScope = CoroutineScope(Dispatchers.IO)
    fun init() {
        jsonParser = Json {
            ignoreUnknownKeys = true
        }
        FirewatchData.targets.forEach {
            FirewatchData.lastUpdateTime.putIfAbsent(it.key,Clock.System.now())
        }
        //TODO: test target validity on init (one-time operation, probably by lazy)
        job = watcherScope.launch {
            while (true) {
                coroutineScope {
                    FirewatchData.targets.forEach {
                        launch {
                            runCatching {
                                checkForUpdate(it.key, it.value)
                            }.onFailure {
                                Firewatch.logger.warning("Error occurred during update check: ${it.message}")
                            }
                        }
                    }
                }
                delay(FirewatchData.updateInterval)
            }
        }
    }

    private suspend fun checkForUpdate(target: PlatformTargetData, contactId: Set<Long>) {
        val localLastUpdateTime = FirewatchData.lastUpdateTime[target] ?: Clock.System.now()
        val resolver = PlatformResolverProvider.resolvePlatformTarget(target.platformIdentifier)
        resolver?.checkForUpdateWrapper(target, localLastUpdateTime,contactId)?.let {
            contactId.forEach { id ->
                Bot.instances[0].getGroup(id)?.sendMessage(it.message)
            }
            FirewatchData.setLastUpdateTime(target, it.lastUpdateTime)
        }
    }

    override fun close() {
        if (::job.isInitialized) {
            job.cancel()
        }
    }
}
