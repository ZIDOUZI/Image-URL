@file:UseSerializers(DateSerializer::class, URLSerializer::class)

package zdz.imageURL

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import zdz.imageURL.process.DateSerializer
import zdz.imageURL.process.URLSerializer
import java.net.URL

@Serializable
data class Data(
    val url: URL,
    @SerialName("assets_url") val assetsURL: URL,
    @SerialName("upload_url") val uploadURL: URL,
    @SerialName("html_url") val htmlUrl: URL,
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
    @SerialName("tarball_url") val tarballUrl: URL,
    @SerialName("zipball_url") val zipballUrl: URL,
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
    @SerialName("avatar_url") val avatarUrl: URL,
    @SerialName("gravatar_id") val gravatarId: String,
    val url: URL,
    @SerialName("html_url") val htmlUrl: URL,
    @SerialName("followers_url") val followersUrl: URL,
    @SerialName("following_url") val followingUrl: URL,
    @SerialName("gists_url") val gistsUrl: URL,
    @SerialName("starred_url") val starredUrl: URL,
    @SerialName("subscriptions_url") val subscriptionsUrl: URL,
    @SerialName("organizations_url") val organizationsUrl: URL,
    @SerialName("repos_url") val reposUrl: URL,
    @SerialName("events_url") val eventsUrl: URL,
    @SerialName("received_events_url") val receivedEventsUrl: URL,
    val type: String,
    @SerialName("site_admin") val siteAdmin: Boolean,
)

@Serializable
data class Asset(
    val url: URL,
    val browser_download_url: URL,
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