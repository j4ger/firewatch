package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.PlatformTargetData
import kotlinx.datetime.Instant
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value


// targets:{
//  <platformTarget>:[<contactId>]
// }
object FirewatchConfigBackend : AutoSavePluginConfig("FirewatchConfig") {
    var targets: MutableMap<String, MutableSet<Long>> by value(mutableMapOf())
    var updateInterval: Long by value(1000.toLong())
}

object FirewatchConfig {
    var targets: MutableMap<PlatformTargetData, MutableSet<Long>>
        get() = FirewatchConfigBackend.targets.mapKeys {
            Firewatch.logger.info("getting")
            return@mapKeys PlatformTargetData.deserialize(it.key)
        }.toMutableMap()
        set(value) {
            FirewatchConfigBackend.targets = value.mapKeys {
                Firewatch.logger.info("setting")
                return@mapKeys PlatformTargetData.serialize(it.key)
            }.toMutableMap()
        }

    var updateInterval: Long
        get() = FirewatchConfigBackend.updateInterval
        set(value) {
            FirewatchConfigBackend.updateInterval = value
        }
}

// lastUpdateTime:{
//  <platformTarget>:<lastUpdateTime>
// }
object FirewatchDataBackend : AutoSavePluginData("FirewatchData") {
    var lastUpdateTime: MutableMap<String, Instant> by value(mutableMapOf())
}

object FirewatchData {
    var lastUpdateTime: MutableMap<PlatformTargetData, Instant>
        get() = FirewatchDataBackend.lastUpdateTime.mapKeys {
            PlatformTargetData.deserialize(it.key)
        }.toMutableMap()
        set(value) {
            FirewatchDataBackend.lastUpdateTime = value.mapKeys {
                PlatformTargetData.serialize(it.key)
            }.toMutableMap()
        }
}