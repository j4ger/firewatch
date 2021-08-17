package cn.j4ger.firewatch

import cn.j4ger.firewatch.SubscribeCommands.subscribeCommandHandler
import cn.j4ger.firewatch.UnsubscribeCommands.unsubscribeCommandHandler
import cn.j4ger.firewatch.platforms.Bilibili
import cn.j4ger.firewatch.platforms.resolvePlatformTarget
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.utils.info

object SubscribeCommands : SimpleCommand(
    Firewatch, "subscribe", "订阅", description = "订阅社交平台更新"
) {
    @Handler
    suspend fun CommandSender.subscribeCommandHandler(targetPlatform: String, targetId: String) {
        //TODO: respawn job
        resolvePlatformTarget(targetPlatform, targetId)?.let {
            val infoResponse: HttpResponse = Watcher.httpClient.get(it.infoRequestUrl)
            if (it.resolveTargetValidity(infoResponse)) {
                val currentGroup = this.getGroupOrNull() ?: kotlin.run {
                    sendMessage("Invalid Command Environment")
                    return@subscribeCommandHandler
                }
                val targetName = it.resolveTargetName(infoResponse)
                val currentSet = FirewatchConfig.targets[it] ?: kotlin.run {
                    mutableSetOf()
                }
                currentSet.add(currentGroup.id)
                FirewatchConfig.targets[it] = currentSet
                sendMessage("已对${it.platformIdentifier} $targetName 添加订阅")

            } else {
                sendMessage("Invalid Target ID")
            } ?: run {
                sendMessage("Unresolved Platform $targetPlatform")
                sendMessage(
                    buildString {
                        appendLine("Supported Platforms:")
                        appendLine("bilibili, bili, b站")
                    }
                )
            }
        }
    }
}

object UnsubscribeCommands : SimpleCommand(
    Firewatch, "unsubscribe", "取消订阅", description = "取消订阅社交平台更新"
) {
    @Handler
    suspend fun CommandSender.unsubscribeCommandHandler(targetPlatform: String, targetId: String) {
        //TODO: respawn job
        resolvePlatformTarget(targetPlatform, targetId)?.let { watcherPlatformTarget ->
            val currentGroup = this.getGroupOrNull() ?: kotlin.run {
                sendMessage("Invalid Command Environment")
                return@unsubscribeCommandHandler
            }
            FirewatchConfig.targets[watcherPlatformTarget]?.let {
                if (it.remove(currentGroup.id)) {
                    FirewatchConfig.targets[watcherPlatformTarget] = it
                    sendMessage("对${watcherPlatformTarget.platformIdentifier} $targetId 的订阅已移除")
                } else {
                    sendMessage("Subscription not found")
                }
            } ?: run {
                sendMessage("Subscription not found")
            }
        } ?: run {
            sendMessage("Unresolved Platform $$targetPlatform")
            sendMessage(
                buildString {
                    appendLine("Supported Platforms:")
                    appendLine("bilibili, bili, b站")
                })
        }
    }
}

object ManageCommands : CompositeCommand(
    Firewatch, "manage", "管理订阅", description = "管理订阅列表"
) {
    @SubCommand("显示全部", "list", "所有订阅", "listall", "全部", "所有", "all")
    suspend fun CommandSender.listAll() {
        Firewatch.logger.info { "listall called" }
        val currentGroup = this.getGroupOrNull() ?: kotlin.run {
            this.sendMessage("Invalid Command Environment")
            return@listAll
        }
        sendMessage(
            buildString {
                appendLine("组${currentGroup.id} 的全部订阅：")
                var total = 0
                FirewatchConfig.targets.forEach {
                    if (currentGroup.id in it.value) {
                        appendLine("${it.key.platformIdentifier} ${it.key.targetName}")
                        total++
                    }
                }
                appendLine("总计$total 项")
            }
        )

    }

}