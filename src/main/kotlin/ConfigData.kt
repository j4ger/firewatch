package cn.j4ger.firewatch

import kotlinx.datetime.Instant
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value


// targets:{
//  <platformTarget>:[<contactId>]
// }
object FirewatchConfig : AutoSavePluginConfig("FirewatchConfig") {
    var targets: MutableMap<WatcherPlatformTarget, MutableSet<Long>> by value(mutableMapOf())
    var updateInterval: Long by value(1000.toLong())
}

// lastUpdateTime:{
//  <platformTarget>:<lastUpdateTime>
// }
object FirewatchData : AutoSavePluginData("FirewatchData") {
    var lastUpdateTime: MutableMap<WatcherPlatformTarget, Instant> by value(mutableMapOf())
}