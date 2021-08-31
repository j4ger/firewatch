# Firewatch

社交媒体搬运机器人插件  
本项目使用 [Mirai框架](https://github.com/mamoe/mirai) API编写，并作为 [Mirai-Console](https://github.com/mamoe/mirai-console) 插件发布  
旨在定时检查社交媒体目标更新，并转发至群聊

## 功能

- [x] 多群聊分别管理配置
- [ ] 多社交平台支持
    - [x] Bilibili
    - [ ] 新浪微博
    - [ ] Twitter
    - [ ] Github
    - [ ] 明日方舟公告
- [x] 聊天环境中配置，无需命令行
- [x] 部署简单（感谢 [Mirai-MCL](https://github.com/iTXTech/mirai-console-loader) 项目）

## 用户指南

### 前期准备

1. 一台闲置的，能够**访问互联网**的设备 

> QQ机器人本质上是一个基于规则自动回复的QQ客户端，因此需要运行的设备。  
> 任何支持运行 [Mirai-Console](https://github.com/mamoe/mirai-console) 的设备都是支持的，你甚至只需要一台闲置的安卓手机。  
> 需要注意机器人提供服务的时候设备需要开机，考虑选择便于**长期运行**的设备。

2. 基本的命令行操作知识

> 只需要会打开命令行，复制粘贴就好啦。

3. 为你的机器人注册一个QQ账号

> 用现有的也不是不可以，不过最好保证**只在**机器人设备上登录。  
> [Mirai](https://github.com/mamoe/mirai) 提供作为多种安卓设备登录的方式，默认为安卓手机，意味着同时允许额外登录一台平板和手表设备。  
> 同一个手机号可以注册多个QQ账号的，所以花点时间重新开一个吧。

5. 把机器人加入需要提供转发服务的群中

> 即使你想先设置好机器人服务再添加至群聊，也请在手机客户端上至少先登录一次账号，这将对后续配置自动登录有很大帮助。  

### 安装 [Mirai-Console](https://github.com/mamoe/mirai-console)

查看 [Mirai用户文档](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md)  

### 安装 [Chat-Command](https://github.com/project-mirai/chat-command)

[Github Releases](https://github.com/project-mirai/chat-command/releases)  
或者在MCL命令行界面执行 `./mcl --update-package net.mamoe:chat-command --channel stable --type plugin`

### 安装 [Firewatch](https://github.com/j4ger/firewatch)

[Github Releases](https://github.com/j4ger/firewatch/releases)  
或者在MCL命令行界面执行 `./mcl --update-package cn.j4ger:firewatch --channel stable --type plugin`

### 运行 [Mirai-Console](https://github.com/mamoe/mirai-console)

如果使用 [Mirai-MCL](https://github.com/iTXTech/mirai-console-loader) 安装，执行 `./mcl` 启动（见 [MCL文档]([Mirai-MCL](https://github.com/iTXTech/mirai-console-loader)) ）  
如果选择手动安装，参考 [Mirai-Console文档](https://github.com/mamoe/mirai-console/blob/master/docs/Run.md#%E5%90%AF%E5%8A%A8-mirai-console-terminal-%E5%89%8D%E7%AB%AF)
运行后Firewatch会在`log`中输出已载入的 [平台解析器](#开发文档) 列表：  
`2021-08-24 00:32:57 I/Firewatch: Loading resolvers: [Bili, Bilibili, 哔哩哔哩]`

### 配置自动登录

在 [Mirai-Console](https://github.com/mamoe/mirai-console) 控制台中执行指令  
`/autoLogin add <account> <password>`

> 记得把 **\<account>** 和 **\<password>** 替换成机器人的QQ号和密码。

用 `/stop` 指令关闭 [Mirai-Console](https://github.com/mamoe/mirai-console) ，再重新启动之  

> 是的，当初的我和你一样以为到这里就结束了。  
> 在一个合理的世界，配置就应该到此为止。  
> 但世界永远不是合理的，至少这个平行宇宙不是。  
> 不出意料地，你会看到类似
> ```
> 2021-08-29 16:30:01 I/Bot.1145141919: [SliderCaptcha] 需要滑动验证码, 请按照以下链接的步骤完成滑动验证码, 然后输入获取到的 ticket
> ```
> 的提示信息，以及给出的几个解决方案的链接。  
> 该处的说明足以提供解决问题的方法，事实上，他们的解释或许有点过于详细了。  
> 在此我推荐查看 [无法登录的临时处理方案](https://mirai.mamoe.net/topic/223/%E6%97%A0%E6%B3%95%E7%99%BB%E5%BD%95%E7%9A%84%E4%B8%B4%E6%97%B6%E5%A4%84%E7%90%86%E6%96%B9%E6%A1%88) 中 **6月6日更新** 部分的说明。  

### 配置命令权限

[Mirai-Console权限系统](https://github.com/mamoe/mirai-console/blob/master/docs/Permissions.md#%E4%BD%BF%E7%94%A8%E5%86%85%E7%BD%AE%E6%9D%83%E9%99%90%E6%9C%8D%E5%8A%A1%E6%8C%87%E4%BB%A4) 默认不允许群成员执行命令  
执行`/perm add * cn.j4ger.firewatch:*`将会允许**所有人**执行**任意Firewatch指令**，如果没有特殊需求，执行这一句指令后就可正常使用插件功能  
详细权限系统用法可参考 [Mirai-Console权限系统](https://github.com/mamoe/mirai-console/blob/master/docs/Permissions.md#%E4%BD%BF%E7%94%A8%E5%86%85%E7%BD%AE%E6%9D%83%E9%99%90%E6%9C%8D%E5%8A%A1%E6%8C%87%E4%BB%A4)   
Firewatch内置的指令列表：

| 指令 | 描述 |
| --- | --- |
| cn.j4ger.firewatch:command.sub | 订阅更新 |
| cn.j4ger.firewatch:command.unsub | 取消订阅更新 |
| cn.j4ger.firewatch:command.manage | 管理订阅列表 |
| cn.j4ger.firewatch:* | 全部指令 |

可对每个指令分别设置权限

### 配置订阅对象

#### 添加订阅

在需要订阅的群中发送 `/sub <平台名称> <目标id>` 添加订阅  
例如：`/sub bilibili 26888199`  
返回：
```
已对Bili 重伤倒地耶格尔添加订阅
```

#### 管理订阅

在已有订阅的群中发送 `/manage list` 管理订阅列表  
示例返回：
```
组690340490的全部订阅：
平台Bili：
#1 重伤倒地耶格尔
总计1项
```

在需要取消订阅的群中发送 `/unsub <平台名称> <目标编号>` 取消订阅  
例如：`/unsub bili 1`  
返回：
```
已取消对Bili 重伤倒地耶格尔的订阅
```

至此，配置就结束了  
理论上一切都应该正常工作了

> 假如出现了不应该发生的事情，欢迎提交Issue反馈

## 开发文档

WIP
