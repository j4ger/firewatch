@file:OptIn(ExperimentalSerializationApi::class)

package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.PlatformTargetData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//TODO: caching and splitting of lastUpdateTime

// targets:{
//  <platformTarget>:[<contactId>]
// }
// lastUpdateTime:{
//  <platformTarget>:<lastUpdateTime>
// }
object FirewatchData {
    private val dataFile = Firewatch.resolveConfigFile("FirewatchConfig.json")
    var targets: MutableMap<PlatformTargetData, MutableSet<Long>> = mutableMapOf()

    // default update interval is set to be 1 min
    var updateInterval: Long = 60000
        set(value) {
            field = value
            save()
        }

    var lastUpdateTime: MutableMap<PlatformTargetData, Instant> = mutableMapOf()

    fun setLastUpdateTime(target: PlatformTargetData, newValue: Instant) {
        lastUpdateTime[target] = newValue
        save()
    }

    fun addSubscriber(target: PlatformTargetData, newSubscriber: Long) {
        val currentSet = targets[target] ?: run {
            mutableSetOf()
        }
        currentSet.add(newSubscriber)
        targets[target] = currentSet
        lastUpdateTime.putIfAbsent(target, Clock.System.now())
        save()
    }

    fun removeSubscriber(target: PlatformTargetData, subscriber: Long): Boolean {
        targets[target]?.let {
            if (it.remove(subscriber)) {
                if (it.isEmpty()) {
                    targets.remove(target)
                } else {
                    targets[target] = it
                }
                save()
                return true
            }
        }
        return false
    }

    fun reload() {
        try {
            val jsonContent = dataFile.readText()
            Json.decodeFromString<FirewatchDataJson?>(jsonContent)?.let { dataJson ->
                targets = dataJson.targets.mapKeys {
                    PlatformTargetData.deserialize(it.key)
                }.mapValues {
                    it.value.toMutableSet()
                }.toMutableMap()
                updateInterval = dataJson.updateInterval
                lastUpdateTime = dataJson.lastUpdateTime.mapKeys {
                    PlatformTargetData.deserialize(it.key)
                }.toMutableMap()
            }
        } catch (exception: Exception) {
            Firewatch.logger.warning(exception.message)
            Firewatch.logger.info("Fresh run. Initializing configs.")
        }
    }

    fun save() {
        val content = FirewatchDataJson(
            targets.mapKeys {
                it.key.serialize()
            }, updateInterval, lastUpdateTime.mapKeys {
                it.key.serialize()
            }
        )
        Firewatch.logger.info("Saving to $dataFile")
        Firewatch.logger.info(content.toString())
        val jsonContent = Json.encodeToString(content)
        dataFile.writeText(jsonContent)
    }

    @Serializable
    data class FirewatchDataJson(
        val targets: Map<String, Set<Long>>,
        val updateInterval: Long,
        val lastUpdateTime: Map<String, Instant>
    )
}
