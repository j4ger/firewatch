package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.WatcherPlatformTarget

fun resolvePlatformTarget(targetPlatform: String, targetId: String): WatcherPlatformTarget? {
    return when (targetPlatform.lowercase()) {
        "bilibili", "bili", "b站" -> {
            Bilibili(targetId)
        }
        else -> null
    }
}