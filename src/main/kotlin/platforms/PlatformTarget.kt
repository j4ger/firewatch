package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.Watcher
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.Instant
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource

/**
 * The data class used in config files as well as update function calls
 *
 * To avoid confusion and misfunctioning the constructor should not be called directly
 *
 * Use [PlatformResolver.buildPlatformTarget] instead
 */
class PlatformTargetData
@Deprecated(message="Manual construction may cause confusion",replaceWith = ReplaceWith("PlatformResolver.buildPlatformTarget(name,params)","cn.j4ger.firewatch.PlatformResolver.buildPlatformTarget"),level = DeprecationLevel.WARNING)
constructor(
    val platformIdentifier: String,
    val name: String,
    val params: List<String> = listOf()
) {
    override fun hashCode(): Int =
        serialize().hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlatformTargetData

        return serialize()==other.serialize()
    }

    companion object {
        fun deserialize(source: String): PlatformTargetData {
            source.split(":-:").let {
                val paramList = it[2].drop(1).dropLast(1).split(",").toMutableList()
                @Suppress("DEPRECATION")
                return@deserialize PlatformTargetData(it[0], it[1], paramList)
            }
        }
    }

    fun serialize(): String {
        return "${this.platformIdentifier}:-:${this.name}:-:${this.params}"
    }

}

/**
 * The data class used to report updates
 *
 * @param[lastUpdateTime] The time of last update message
 *
 * @param[message] A [net.mamoe.mirai.message.data.Message] object to report the updates to the user
 */
data class UpdateInfo(
    val lastUpdateTime: Instant,
    val message: Message
)

/**
 * Generic platform resolver base class
 *
 * Implement this class to add support for new social platforms
 *
 * A resolver instance is created stateless-ly on load so no constructor is needed
 */
abstract class PlatformResolver {
    /**
     * A set of strings which identifies the name of each platform
     *
     * This is used to parse user input and generate config files
     *
     * Letter case is irrelevant
     */
    abstract val platformIdentifier: Set<String>

    /**
     * Resolve a platform target based on user input
     *
     * @param[params] A list of params the user enters, the first element is considered to be the platform identifier
     *
     * Example: `/sub bili 114514 1919810` command will be parsed into `["bili",114514,1919810]`
     *
     * @return A [PlatformTargetData] object, constructed using [PlatformResolver.buildPlatformTarget]
     *
     * If no target can be resolved, null should be returned
     */
    abstract suspend fun resolveTarget(params: List<String>): PlatformTargetData?

    /**
     * Checks for platform-specific updates
     *
     * @param[platformTargetData] The PlatformTargetData object created by [resolveTarget] method
     *
     * @param[lastUpdateTime] The time of last reported update, used to filter which updates should be returned
     *
     * @return An [UpdateInfo] object
     *
     * If no update is found, null should be returned
     */
    abstract suspend fun checkForUpdate(platformTargetData: PlatformTargetData, lastUpdateTime: Instant): UpdateInfo?

    /**
     * Function used to upload image resource and get [net.mamoe.mirai.message.data.Image] objects
     *
     * @param[sourceUrl] The source URL to an image
     *
     * @param[filename] The filename for the target image, default to the result of URL parsing if unset\
     *
     * @return A [net.mamoe.mirai.message.data.Image] object, used to construct [net.mamoe.mirai.message.data.Message]s
     *
     * Note that if no extension is found in filename, the output image will have a ".mirai" extension
     */
    @Suppress("UNUSED")
    suspend fun uploadImage(sourceUrl: String,filename:String?): Image {
        val imageResponse: HttpResponse = Watcher.httpClient.get(sourceUrl)
        val imageBytes: ByteArray = imageResponse.receive()
        val externalResource = imageBytes.toExternalResource(filename?:sourceUrl.substring(sourceUrl.lastIndexOf("/")))
        return (Bot.instances[0].asFriend as Contact).uploadImage(externalResource)
    }

    /**
     * Builds a [PlatformTargetData]
     *
     * @param[name] The name of the target, which is shown to the user
     *
     * @param[params] A list of internal params, typically target id and platform-specific information
     *
     * @return A [PlatformTargetData] object, which will be passed in when [PlatformResolver.checkForUpdate] is called
     */
    protected fun buildPlatformTarget(name:String,params:List<String>) : PlatformTargetData =
        @Suppress("DEPRECATION")
        PlatformTargetData(this.platformIdentifier.first(),name,params.toMutableList())

    /**
     * The ktor httpClient for making http requests
     *
     * This should be the only client used in order to keep a unified lifecycle management working
     *
     * Refer to [io.ktor.client.HttpClient] for detailed usage
     */
    val httpClient = Watcher.httpClient

    /**
     * A simple Json parser with [kotlinx.serialization.json.JsonBuilder.ignoreUnknownKeys] enabled
     *
     * Refer to [kotlinx.serialization.json.Json] for detailed usage
     *
     * Most possibly you would want to check out [kotlinx.serialization.json.Json.decodeFromString]
     */
    @Suppress("UNUSED")
    val jsonParser = Watcher.jsonParser
}