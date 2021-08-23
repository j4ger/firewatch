package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.PlatformResolverProvider
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
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
        FirewatchData.reload()

        UnifiedSubscribeCommand.register()
        UnifiedUnsubscribeCommand.register()
        ManageCommands.register()

        Watcher.init()

        logger.info("Loading resolvers: ${PlatformResolverProvider.getAvailablePlatforms()}")
        logger.info { "Plugin enabled" }
    }

    override fun onDisable() {
        FirewatchData.save()

        super.onDisable()
        Watcher.close()

        UnifiedSubscribeCommand.unregister()
        UnifiedUnsubscribeCommand.unregister()
        ManageCommands.unregister()

        logger.info { "Plugin disabled" }
    }
}