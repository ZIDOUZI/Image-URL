package zdz.imageURL.activity.main

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import zdz.imageURL.R
import zdz.imageURL.model.Logger
import zdz.imageURL.model.data.Data
import zdz.imageURL.model.data.Prefer
import zdz.imageURL.model.data.Type
import zdz.imageURL.utils.download
import zdz.imageURL.utils.downloadImage
import zdz.imageURL.utils.getContentUri
import zdz.imageURL.utils.mimeType
import zdz.imageURL.utils.openAfterFinished
import zdz.imageURL.utils.parseHeadedIdString
import zdz.imageURL.utils.parseUrlString
import zdz.imageURL.utils.saveImage
import zdz.imageURL.utils.sendImage
import zdz.imageURL.utils.sendText
import zdz.imageURL.utils.subFile
import zdz.imageURL.utils.viewImage
import zdz.imageURL.utils.viewUri
import zdz.imageURL.utils.with
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val pf: Prefer,
    val logger: Logger,
    private val downloadManager: DownloadManager
) : ViewModel() {
    
    private val client = HttpClient(Android) {
        engine {
            connectTimeout = 0
        }
        install(ContentNegotiation) {
            json()
        }
    }
    
    suspend fun getData(): Data = client.get(updateUrl).body<Data>()
    
    private var rootDir: DocumentFile? = null
    
    fun getRootDir(ctx: Context) =
        rootDir ?: ctx.contentResolver.persistedUriPermissions.firstOrNull()?.let {
            DocumentFile.fromTreeUri(ctx, it.uri)
        }.also { rootDir = it }
    
    private val updateUrl = context.getString(R.string.update_url)
    private val appName = context.getString(R.string.app_name)
    private val shareUrl = context.getString(R.string.share_url)
    private val feedbackUrl = Uri.parse(context.getString(R.string.feedback_url))
    
    fun sendShareLink(ctx: Context) = ctx.sendText(shareUrl)
    fun openFeedback(ctx: Context) = ctx.viewUri(feedbackUrl)
    
    suspend fun downloadUpdate(data: Data, ctx: Context) = downloadManager.download(
        data.assets.first().browser_download_url.toString(),
        subPath = "${appName}_release_${getData().tagName}.apk"
    ).let { downloadManager.openAfterFinished(ctx, it) }
    
    suspend fun parseTextInput(s: String): Pair<Type, String>? =
        s.parseUrlString() ?: s.parseHeadedIdString() ?: s.trim().takeIf { it.isDigitsOnly() }
            ?.let { id -> pf.preferredID.current()?.with { it.url(id) } }
    
    suspend fun downloadImage(url: Url): Triple<Bitmap, String, CompressFormat?>? =
        client.downloadImage(url, logger)
    
    private suspend fun cacheImage(ctx: Context, bitmap: Bitmap): Uri? = ctx.externalCacheDir?.let {
        DocumentFile.fromFile(it)
    }?.subFile("cacheFile", "image/png")?.let {
        logger.measureTimeMillis("cached in %d millis time.") {
            it.saveImage(ctx, bitmap, CompressFormat.PNG)
        }
    }?.let { ctx.getContentUri(it.uri) }
    
    fun viewImage(scope: CoroutineScope, ctx: Context, bitmap: Bitmap) = scope.launch {
        cacheImage(ctx, bitmap)?.let { ctx.viewImage(it, pf.imageChooser.current()) }
    }
    
    fun shareImage(scope: CoroutineScope, ctx: Context, bitmap: Bitmap) = scope.launch {
        cacheImage(ctx, bitmap)?.let(ctx::sendImage)
    }
    
    suspend fun saveImage(
        ctx: Context, bitmap: Bitmap, name: String, format: CompressFormat
    ): Uri? = getRootDir(ctx)?.subFile(name, format.mimeType)?.let {
        logger.measureTimeMillis("saved image in %d millis time.") {
            it.saveImage(ctx, bitmap, format)
        }
    }?.uri
    
}