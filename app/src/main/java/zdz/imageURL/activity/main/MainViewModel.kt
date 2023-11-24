package zdz.imageURL.activity.main

import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zdz.imageURL.R
import zdz.imageURL.model.Logger
import zdz.imageURL.model.data.Data
import zdz.imageURL.model.data.Prefer
import zdz.imageURL.model.data.Type
import zdz.imageURL.utils.download
import zdz.imageURL.utils.downloadImage
import zdz.imageURL.utils.getContentUri
import zdz.imageURL.utils.getSourceCode
import zdz.imageURL.utils.mimeType
import zdz.imageURL.utils.openAfterFinished
import zdz.imageURL.utils.parseHeadedIdString
import zdz.imageURL.utils.parseUrlString
import zdz.imageURL.utils.saveImage
import zdz.imageURL.utils.sendImage
import zdz.imageURL.utils.sendText
import zdz.imageURL.utils.subFile
import zdz.imageURL.utils.toast
import zdz.imageURL.utils.urlReg
import zdz.imageURL.utils.viewImage
import zdz.imageURL.utils.viewUri
import zdz.imageURL.utils.with
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val pf: Prefer,
    val logger: Logger,
    private val downloader: DownloadManager,
    private val clipboard: ClipboardManager
) : ViewModel() {
    var text by mutableStateOf("")
    var count by mutableIntStateOf(0)
    
    var sourceUrl by mutableStateOf<String?>(null)
    var imgUrl by mutableStateOf<String?>(null)
    var bitmap by mutableStateOf<Bitmap?>(null)
    
    private var format = CompressFormat.PNG
    private suspend fun getFormat() = pf.preferredMimeType.current() ?: format
    private var filename = "image.png"
    
    var error by mutableStateOf(true)
    
    var data: Data? by mutableStateOf(null)
    
    suspend fun checkUpdate() = try {
        logger.measureTimeMillis("get remove info in %d millis time") {
            getData()
        }.run {
            isOutOfData().also { if (it) data = this }
        }
    } catch (e: Throwable) {
        logger.e(e)
        null
    }
    
    private suspend fun getData(): Data = client.get(updateUrl).body<Data>()
    
    private var rootDir: DocumentFile? = null
    
    fun getRootDir(ctx: Context) =
        rootDir ?: ctx.contentResolver.persistedUriPermissions.singleOrNull()?.let {
            DocumentFile.fromTreeUri(ctx, it.uri)
        }.also { rootDir = it }
    
    private val appName = context.getString(R.string.app_name)
    
    fun sendShareLink(ctx: Context) = ctx.sendText(shareUrl)
    fun openFeedback(ctx: Context) = ctx.viewUri(feedbackUrl)
    
    suspend fun downloadUpdate(data: Data, ctx: Context) = downloader.download(
        data.assets.first().browser_download_url.toString(),
        subPath = "${appName}_release_${getData().tagName}.apk"
    ).let { downloader.openAfterFinished(ctx, it, choose = pf.installerChooser.current()) }
    
    private suspend fun parseTextInput(s: String): Pair<Type, String>? =
        if (urlReg in s) s.parseUrlString() else // to make sure don't parse id if contains url.
            s.parseHeadedIdString() ?: s.trim().takeIf { it.isDigitsOnly() }
                ?.let { id -> pf.preferredID.current()?.with { it.url(id) } }
    
    private suspend fun downloadImage(url: Url): Triple<Bitmap, String, CompressFormat?>? =
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
        ctx: Context, bitmap: Bitmap
    ): Uri? = getRootDir(ctx)?.subFile(filename, getFormat().mimeType)?.let {
        logger.measureTimeMillis("saved image in %d millis time.") {
            it.saveImage(ctx, bitmap, getFormat())
        }
    }?.uri
    
    /**
     * All the process of user input will be processed here.
     */
    suspend fun process(ctx: Context) {
        error = true
        if (text.isBlank()) return
        
        if (text == "7399608") {
            count = 114514
            error = false
            return
        }
        
        delay(1000) // TODO: 选择修改延时时间
        val (type, url) = parseTextInput(text) ?: return
        
        if (type is Type.Unknown) return
        
        error = false
        
        sourceUrl = url
        if (type !is Type.Extractable) {
            imgUrl = null
            if (pf.autoJump.current()) ctx.viewUri(Uri.parse(url), pf.jumpChooser.current())
            return
        }
        
        try {
            Url(url).getSourceCode()
        } catch (e: Throwable) {
            if (e is SocketTimeoutException) ctx.toast("timeout")
            logger.e("get source code error.", e)
            error = true
            return
        }.let(type::extractImageUrl).let { imageUrl ->
            imgUrl = imageUrl
            try {
                downloadImage(Url(imageUrl))
            } catch (e: Throwable) {
                logger.e("download image error.", e)
                error = true
                return
            }
        }?.let { (b, name, compressFormat) ->
            bitmap = b
            format = compressFormat ?: CompressFormat.PNG
            filename = name
        } ?: Unit.run {
            logger.e("Can't decode bitmap")
            error = true
        }
    }
    
    @Composable
    fun Refresh(context: Context) = LaunchedEffect(key1 = text) {
        process(context)
    }
    
    fun processClipboard(): Unit? = clipboard.primaryClip?.getItemAt(0)?.let {
        it.text?.toString() ?: it.uri?.toString()
    }?.let { text = it }
    
    private companion object {
        const val updateUrl = "https://api.github.com/repos/ZIDOUZI/Image-URL/releases/latest"
        const val shareUrl = "https://github.com/ZIDOUZI/Image-URL"
        val feedbackUrl = Uri.parse("https://github.com/ZIDOUZI/Image-URL/issues/new")
        
        val client = HttpClient(Android) {
            engine {
                connectTimeout = 0
            }
            install(ContentNegotiation) {
                json()
            }
        }
    }
    
}