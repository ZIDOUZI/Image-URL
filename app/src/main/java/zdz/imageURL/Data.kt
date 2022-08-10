@file:UseSerializers(DateSerializer::class, UrlSerializer::class)

package zdz.imageURL

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import zdz.imageURL.process.DateSerializer
import zdz.imageURL.process.UrlSerializer

@Serializable
data class Data(
    val url: Url,
    @SerialName("assets_url") val assetsUrl: Url,
    @SerialName("upload_url") val uploadUrl: Url,
    @SerialName("html_url") val htmlUrl: Url,
    val id: Long,
    val author: User,
    @SerialName("node_id") val nodeId: String,
    @SerialName("tag_name") val tagName: String,
    @SerialName("target_commitish") val targetCommitish: String,
    val name: String,
    val draft: Boolean,
    val prerelease: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("published_at") val publishedAt: String,
    val assets: List<Asset>,
    @SerialName("tarball_url") val tarballUrl: Url,
    @SerialName("zipball_url") val zipballUrl: Url,
    val body: String,
) {
    fun isOutOfData(version: String): Boolean {
        val a = tagName.split('.')
        val b = version.split('.')
        (a zip b).forEach { (i, j) ->
            if (i > j) return true
        }
        return tagName > version
    }
}

@Serializable
data class User(
    val login: String,
    val id: Long,
    @SerialName("node_id") val nodeId: String,
    @SerialName("avatar_url") val avatarUrl: Url,
    @SerialName("gravatar_id") val gravatarId: String,
    val url: Url,
    @SerialName("html_url") val htmlUrl: Url,
    @SerialName("followers_url") val followersUrl: Url,
    @SerialName("following_url") val followingUrl: Url,
    @SerialName("gists_url") val gistsUrl: Url,
    @SerialName("starred_url") val starredUrl: Url,
    @SerialName("subscriptions_url") val subscriptionsUrl: Url,
    @SerialName("organizations_url") val organizationsUrl: Url,
    @SerialName("repos_url") val reposUrl: Url,
    @SerialName("events_url") val eventsUrl: Url,
    @SerialName("received_events_url") val receivedEventsUrl: Url,
    val type: String,
    @SerialName("site_admin") val siteAdmin: Boolean,
)

@Serializable
data class Asset(
    val url: Url,
    val browser_download_url: Url,
    val id: Long,
    @SerialName("node_id") val nodeId: String,
    val name: String,
    val label: String?,
    val state: String,
    @SerialName("content_type") val contentType: String,
    val size: Long,
    @SerialName("download_count") val downloadCount: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("uploader") val uploader: User,
)