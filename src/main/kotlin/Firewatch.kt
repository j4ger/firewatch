package cn.j4ger.firewatch

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAll
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
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
        FirewatchConfig.reload()
        FirewatchData.reload()

        SubscribeCommands.register()
        UnsubscribeCommands.register()
        ManageCommands.register()

        Watcher.init()
        logger.info { "Plugin enabled" }
    }

    override fun onDisable() {
        super.onDisable()
        Watcher.close()

        SubscribeCommands.unregister()
        UnsubscribeCommands.unregister()
        ManageCommands.unregister()
        logger.info { "Plugin disabled" }
    }
}