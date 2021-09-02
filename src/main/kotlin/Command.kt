package cn.j4ger.firewatch

import cn.j4ger.firewatch.platforms.PlatformResolverProvider
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content

object UnifiedSubscribeCommand : RawCommand(
    Firewatch, "sub","subscribe", description = "订阅更新"
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
                sendMessage("已对${it.platformIdentifier} ${it.name}添加订阅")
            } ?: run {
                sendMessage("Invalid Target \"${params}\"")
            }
        } ?: run {
            sendMessage("Invalid Platform \"${params[0]}\"")
            sendMessage(
                buildString {
                    appendLine("Available Platforms:")
                    appendLine(PlatformResolverProvider.getAvailablePlatforms())
                }.trim()
            )
        }
    }
}

object UnifiedUnsubscribeCommand:SimpleCommand(
    Firewatch,"unsub","unsubscribe",description = "取消订阅更新"
){
    @Handler
    suspend fun CommandSender.onUnsubscribe(platform:String,targetId:Int){
        val currentGroup = this.getGroupOrNull() ?: run {
            sendMessage("Invalid Command Environment")
            return@onUnsubscribe
        }
        PlatformResolverProvider.resolvePlatformTarget(platform)?.let {resolver ->
            FirewatchData.targets.filter{
                it.value.contains(currentGroup.id)
            }.keys.filter {
                it.platformIdentifier ==  resolver.platformIdentifier.first()
            }.sortedBy {
                it.name
            }[targetId-1].let {
                if (FirewatchData.removeSubscriber(it,currentGroup.id)){
                    sendMessage("已取消对${it.platformIdentifier} ${it.name}的订阅")
                } else {
                    sendMessage("Invalid target id \"${targetId}\"")
                }
            }?: run{
                sendMessage("Invalid target id \"${targetId}\"")
            }
        }?: run{
            sendMessage("Invalid platform \"${platform}\"")
        }
    }
}

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
                appendLine("组${currentGroup.id}的全部订阅：")
                var total = 0
                println("${FirewatchData.targets.keys}")
                FirewatchData.targets.keys.groupBy {
                    it.platformIdentifier
                }.forEach{ platform ->
                    appendLine("平台${platform.key}：")
                    var subTotal = 0
                    platform.value.sortedBy {
                        it.name
                    }.forEach {
                        if (FirewatchData.targets[it]?.contains(currentGroup.id) == true){
                            subTotal++
                            appendLine("#${subTotal} ${it.name}")
                        }
                    }
                    total+=subTotal
                }
                appendLine("总计${total}项")
            }.trim()
        )

    }
}

// TODO: usage, help command
