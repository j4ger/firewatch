package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.Bilibili
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.getGroupOrNull

object SubscribeCommands : SimpleCommand(
    Firewatch, "订阅", "subscribe", description = "订阅社交平台更新"
) {
    @Handler
    suspend fun CommandSender.subscribeCommandHandler(targetPlatform: String, targetId: String) {
        when (targetPlatform.lowercase()) {
            "bilibili", "bili", "b站" -> {
                val newTarget = Bilibili(targetId)
                val infoResponse: HttpResponse = Watcher.httpClient.get(newTarget.infoRequestUrl)
                if (newTarget.resolveTargetValidity(infoResponse)) {
                    val currentGroup = this.getGroupOrNull() ?: kotlin.run {
                        this.sendMessage("Invalid Command Environment")
                        return@subscribeCommandHandler
                    }
                    val targetName = newTarget.resolveTargetName(infoResponse)
                    val currentList = FirewatchConfig.targets[newTarget] ?: kotlin.run {
                        mutableSetOf()
                    }
                    currentList.add(currentGroup.id)
                    this.sendMessage("已对${newTarget.platformIdentifier} $targetName 添加订阅")
                } else {
                    this.sendMessage("Invalid Target ID")
                }
            }
            else -> {
                this.sendMessage("Unresolved Platform")
                this.sendMessage(
                    buildString {
                        appendLine("Supported Platforms:")
                        appendLine("bilibili, bili, b站")
                    }
                )
            }
        }
    }
}

object UnsubscribeCommands : CompositeCommand(
    Firewatch, "取消订阅", "unsubscribe", description = "取消订阅社交平台更新"
) {
    @SubCommand("微博", "weibo", "Weibo")
    suspend fun CommandSender.unsubscribeWeibo(target: String) {
        TODO("needs further implementations")
    }

    @SubCommand("B站", "b站", "哔哩哔哩", "bilibili", "bili")
    suspend fun CommandSender.unsubscribeBilibili(target: String) {
        TODO("needs further implementations")
    }

    @SubCommand("编号", "id")
    suspend fun CommandSender.unsubscribeId(target: String) {
        TODO("needs further implementations")
    }

}

object ManageCommands : CompositeCommand(
    Firewatch, "管理订阅", "manage-subscription", description = "管理订阅列表"
) {
    @SubCommand("显示全部", "list-all")
    suspend fun CommandSender.listAll() {
        TODO("needs further implementations")
    }

}