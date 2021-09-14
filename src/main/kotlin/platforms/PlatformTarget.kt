package cn.j4ger.firewatch.platforms

import cn.j4ger.firewatch.Firewatch
import cn.j4ger.firewatch.Watcher
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitByteArray
import kotlinx.datetime.Instant
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.warning

/**
 * The data class used in config files as well as update function calls
 *
 * To avoid confusion and misfunctioning the constructor should not be called directly
 *
 * Use [PlatformResolver.buildPlatformTarget] instead
 */
class PlatformTargetData
@Deprecated(
    message = "Manual construction may cause confusion",
    replaceWith = ReplaceWith(
        "PlatformResolver.buildPlatformTarget(name,params)",
        "cn.j4ger.firewatch.PlatformResolver.buildPlatformTarget"
    ),
    level = DeprecationLevel.WARNING
)
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

        return serialize() == other.serialize()
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
 * Internal class for handling image uploads
 *
 * Due to limits of Mirai, all update check functions must be extension functions of this class
 */
class GroupTarget(private val groupId:Set<Long>){

    /**
     * Function for uploading image resources
     *
     * @param[sourceUrl] The source URL to an image
     *
     * @return A [net.mamoe.mirai.message.data.Message] object, used to construct [net.mamoe.mirai.message.data.Message]s
     *
     * Note that if no extension is found in filename or parsed from file header, the output image will have a ".mirai" extension which may not be displayed correctly
     */
    suspend fun uploadImage(sourceUrl: String): Message {
        try {
            val fileExtension = sourceUrl.substringAfterLast(".","")
            val imageBytes = Fuel.get(sourceUrl).awaitByteArray()
            imageBytes.toExternalResource(
                fileExtension
            ).use {
                return@uploadImage it.uploadAsImage(Bot.instances[0].getGroupOrFail(groupId.first()))
            }
        } catch (exception: Exception) {
            Firewatch.logger.warning("Image upload failed:${sourceUrl} with error:${exception}")
            return PlainText("<Invalid Image:${sourceUrl}>")
        }
    }
}

/**
 * Generic platform resolver base class
 *
 * Implement this class to add support for new social media platforms
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
    abstract suspend fun GroupTarget.checkForUpdate(platformTargetData: PlatformTargetData, lastUpdateTime: Instant): UpdateInfo?

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
     * Builds a [PlatformTargetData]
     *
     * @param[name] The name of the target, which is shown to the user
     *
     * @param[params] A list of internal params, typically target id and platform-specific information
     *
     * @return A [PlatformTargetData] object, which will be passed in when [PlatformResolver.checkForUpdate] is called
     */
    protected fun buildPlatformTarget(name: String, params: List<String>): PlatformTargetData =
        @Suppress("DEPRECATION")
        PlatformTargetData(this.platformIdentifier.first(), name, params.toMutableList())

    /**
     * A simple Json parser with [kotlinx.serialization.json.JsonBuilder.ignoreUnknownKeys] enabled
     *
     * Refer to [kotlinx.serialization.json.Json] for detailed usage
     *
     * Most possibly you would want to check out [kotlinx.serialization.json.Json.decodeFromString]
     */
    @Suppress("UNUSED")
    val jsonParser = Watcher.jsonParser

    suspend fun checkForUpdateWrapper(platformTargetData: PlatformTargetData, lastUpdateTime: Instant,contactId:Set<Long>):UpdateInfo?
        =GroupTarget(contactId).checkForUpdate(platformTargetData,lastUpdateTime)
}
