package cn.j4ger.firewatch.platforms

import org.reflections.Reflections
import kotlin.reflect.full.createInstance

object PlatformResolverProvider {
    private var candidates: Set<PlatformResolver>

    init {
        val reflections = Reflections("cn.j4ger.firewatch.platforms")
        val resolverTypes = reflections.getSubTypesOf(PlatformResolver::class.java)
        candidates = resolverTypes.map {
            it.kotlin.createInstance()
        }.toSet()
    }

    fun getAvailablePlatforms(): String {
        return buildString {
            candidates.forEach {
                appendLine(it.platformIdentifier.toString())
            }
        }
    }

    fun resolvePlatformTarget(targetPlatform: String): PlatformResolver? {
        candidates.forEach { platformResolver ->
            if (targetPlatform.lowercase() in platformResolver.platformIdentifier.map { it.lowercase() }) {
                return@resolvePlatformTarget platformResolver
            }
        }
        return null
    }
}

