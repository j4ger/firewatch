package cn.j4ger.firewatch

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import java.time.LocalDateTime


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
    var lastUpdateTime: MutableMap<WatcherPlatformTarget, LocalDateTime> by value(mutableMapOf())
}