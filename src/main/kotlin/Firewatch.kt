package cn.j4ger

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object Firewatch : KotlinPlugin(
    JvmPluginDescription(
        id = "cn.j4ger.firewatch",
        name = "Firewatch",
        version = "1.0-SNAPSHOT",
    ) {
        author("J4ger")
        info("""Social media update watcher""")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
    }
}