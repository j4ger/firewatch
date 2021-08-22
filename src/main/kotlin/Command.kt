package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.PlatformResolverProvider
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content

object UnifiedSubscribeCommands : RawCommand(
    Firewatch, "sub", description = "订阅更新"
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val params = args.map {
            it.content.split(" ")
        }.flatten()
        PlatformResolverProvider.resolvePlatformTarget(params[0])?.let { platformResolver ->
            platformResolver.resolveTarget(params)?.let {
                val currentGroup = this.getGroupOrNull() ?: run {
                    sendMessage("Invalid Command Environment")
                    return@onCommand
                }
                FirewatchData.addSubscriber(it, currentGroup.id)
                sendMessage("已对${it.platformIdentifier} ${it.name} 添加订阅")
            } ?: run {
                sendMessage("Invalid Target $params")
            }
        } ?: run {
            sendMessage("Invalid Platform ${params[0]}")
            sendMessage(
                buildString {
                    appendLine("Available Platforms:")
                    appendLine(PlatformResolverProvider.getAvailablePlatforms())
                }.trim()
            )
        }
    }
}

//object UnsubscribeCommands : SimpleCommand(
//    Firewatch, "unsubscribe", "取消订阅", description = "取消订阅社交平台更新"
//) {
//    @Handler
//    suspend fun CommandSender.unsubscribeCommandHandler(targetPlatform: String, targetId: String) {
//        //TODO: respawn job
//        resolvePlatformTarget(targetPlatform, targetId)?.let { watcherPlatformTarget ->
//            val currentGroup = this.getGroupOrNull() ?: run {
//                sendMessage("Invalid Command Environment")
//                return@unsubscribeCommandHandler
//            }
//            FirewatchConfig.targets[watcherPlatformTarget]?.let {
//                if (it.remove(currentGroup.id)) {
//                    FirewatchConfig.targets[watcherPlatformTarget] = it
//                    sendMessage("对${watcherPlatformTarget.platformIdentifier} $targetId 的订阅已移除")
//                } else {
//                    sendMessage("Subscription not found")
//                }
//            } ?: run {
//                sendMessage("Subscription not found")
//            }
//        } ?: run {
//            sendMessage("Unresolved Platform $$targetPlatform")
//            sendMessage(
//                buildString {
//                    appendLine("Supported Platforms:")
//                    appendLine("bilibili, bili, b站")
//                })
//        }
//    }
//}
//
object ManageCommands : CompositeCommand(
    Firewatch, "manage", "管理订阅", description = "管理订阅列表"
) {
    @SubCommand("显示全部", "list", "所有订阅", "listall", "全部", "所有", "all")
    suspend fun CommandSender.listAll() {
        val currentGroup = this.getGroupOrNull() ?: run {
            this.sendMessage("Invalid Command Environment")
            return@listAll
        }
        sendMessage(
            buildString {
                appendLine("组${currentGroup.id} 的全部订阅：")
                var total = 0
                FirewatchData.targets.forEach {
                    if (currentGroup.id in it.value) {
                        appendLine("${it.key.platformIdentifier} ${it.key.name}")
                        total++
                    }
                }
                appendLine("总计$total 项")
            }.trim()
        )

    }

}