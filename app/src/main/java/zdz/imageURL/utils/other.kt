package zdz.imageURL.utils

import android.app.Activity
import android.app.DownloadManager
import android.app.DownloadManager.COLUMN_MEDIA_TYPE
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.core.content.FileProvider
import androidx.datastore.preferences.preferencesDataStore
import androidx.documentfile.provider.DocumentFile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import zdz.imageURL.BuildConfig
import zdz.imageURL.model.Logger
import java.io.File
import java.io.InputStream

val Context.dataStore by preferencesDataStore("settings")

inline fun <T, R> T.with(transform: (T) -> R) = to(transform(this))

inline fun <T, R> T.by(transform: (T) -> R) = transform(this) to this

fun Activity.toast(text: String, duration: Int = Toast.LENGTH_SHORT) = runOnUiThread {
    Toast.makeText(this, text, duration).show()
}

suspend fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) =
    withContext(AndroidUiDispatcher.Main) {
        Toast.makeText(this@toast, text, duration).show()
    }

suspend fun Context.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_SHORT) =
    withContext(AndroidUiDispatcher.Main) {
        Toast.makeText(this@toast, textId, duration).show()
    }

const val fileProviderAuthority: String = "${BuildConfig.APPLICATION_ID}.fileProvider"

fun Context.getContentUri(file: File): Uri =
    FileProvider.getUriForFile(this, fileProviderAuthority, file)

fun Context.getContentUri(uri: Uri): Uri =
    FileProvider.getUriForFile(this, fileProviderAuthority, File(uri.path!!))

/*suspend fun Context.shareImage() = withContext(Dispatchers.IO) {
    // 防止uri泄露.save()方法的uri以file://开头,应转化为content://开头
    val uri = FileProvider.getUriForFile(
        this@shareImage, "${BuildConfig.APPLICATION_ID}.fileProvider", saveAsync().await().toFile()
    )
    Intent(Intent.ACTION_SEND).apply {
        type = "image*//*"
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }.let {
        startActivity(Intent.createChooser(it, "分享图片"))
    }
}*/

fun Context.copy(res: Any?) {
    val cm = getSystemService(ComponentActivity.CLIPBOARD_SERVICE) as ClipboardManager
    // 创建普通字符型ClipData
    val clipData = ClipData.newPlainText("copy image link", res.toString())
    // 将ClipData内容放到系统剪贴板里。
    cm.setPrimaryClip(clipData)
    Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
}

fun DownloadManager.download(
    url: String,
    title: String = "下载文件",
    description: String = "正在下载文件",
    dirType: String = Environment.DIRECTORY_DOWNLOADS,
    subPath: String = "download-file"
) = DownloadManager.Request(Uri.parse(url)).apply {
    setTitle(title)
    setDescription(description)
    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    setDestinationInExternalPublicDir(dirType, subPath)
}.let {
    enqueue(it)
}

/**
 * this method only works when activity is in foreground.
 */
tailrec suspend fun DownloadManager.openAfterFinished(
    ctx: Context,
    id: Long,
    mimeType: String? = null,
    duration: Long = 1000,
    cancelIfPaused: Boolean = true,
    choose: Boolean = false,
    onFailed: (suspend (Long) -> Unit)? = null
) {
    query(id).use {
        require(it.moveToFirst())
        val status = it.getState()
        delay(duration)
        when (status) {
            DownloadManager.STATUS_PENDING, DownloadManager.STATUS_RUNNING -> return@use
            DownloadManager.STATUS_PAUSED -> if (cancelIfPaused) return
            DownloadManager.STATUS_FAILED -> return onFailed?.invoke(id) ?: Unit
            DownloadManager.STATUS_SUCCESSFUL -> {
                val index = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                if (index == -1) return onFailed?.invoke(id) ?: Unit
                ctx.viewContent(
                    Uri.parse(it.getString(index)),
                    mimeType = mimeType ?: it.getString(it.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE)),
                    choose = choose,
                )
            }
        }
        return
    }
    openAfterFinished(ctx, id, mimeType, duration)
}

fun DownloadManager.queryOrNull(id: Long): Cursor? =
    query(DownloadManager.Query().setFilterById(id))

fun DownloadManager.query(id: Long): Cursor =
    queryOrNull(id) ?: throw IllegalArgumentException("can't query id: $id in download manager.")

fun Cursor.getState() = getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

val Bitmap.CompressFormat.mimeType
    get() = when {
        this == Bitmap.CompressFormat.PNG -> "image/png"
        this == Bitmap.CompressFormat.JPEG -> "image/jpeg"
        this == Bitmap.CompressFormat.WEBP -> "image/webp"
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                (this == Bitmap.CompressFormat.WEBP_LOSSLESS || this == Bitmap.CompressFormat.WEBP_LOSSY) -> "image/webp"
        
        else -> throw RuntimeException("An unknown bitmap compress format received: $this")
    }

fun ContentType.getBitmapFormat(lossless: Boolean) = when {
    contentType != "image" -> null
    contentSubtype == "png" -> Bitmap.CompressFormat.PNG
    contentSubtype == "jpeg" -> Bitmap.CompressFormat.JPEG
    contentSubtype == "webp" -> when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> Bitmap.CompressFormat.WEBP
        lossless -> Bitmap.CompressFormat.WEBP_LOSSLESS
        else -> Bitmap.CompressFormat.WEBP_LOSSY
    }
    
    else -> null
}

fun String.getBitmapFormat(lossless: Boolean) = when {
    this == "image/png" -> Bitmap.CompressFormat.PNG
    this == "image/jpeg" -> Bitmap.CompressFormat.JPEG
    this == "image/webp" -> when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> Bitmap.CompressFormat.WEBP
        lossless -> Bitmap.CompressFormat.WEBP_LOSSLESS
        else -> Bitmap.CompressFormat.WEBP_LOSSY
    }
    
    else -> null
}

val compatWebp: Bitmap.CompressFormat
    get() = if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSLESS
    else Bitmap.CompressFormat.WEBP

fun DocumentFile.subFile(name: String, mimeType: String) =
    findFile(name) ?: createFile(mimeType, name)

suspend fun HttpClient.downloadImage(
    url: Url,
    logger: Logger
): Triple<Bitmap, String, Bitmap.CompressFormat?>? =
    logger.measureTimeMillis("downloaded $url in %d millis time.") {
        get(url)
    }.run {
        // TODO: 图像大于15M左右时无法写入bitmap导致闪退.
        // https://stackoverflow.com/questions/42642885
        body<InputStream>().let { BitmapFactory.decodeStream(it) }?.let {
            logger.append("\nbitmap size: ${it.byteCount}, content length: ${contentLength()}")
            val filename = url.pathSegments.last()
            Triple(it, filename, contentType()?.getBitmapFormat(lossless = true))
        }
    }

suspend fun DocumentFile.saveImage(ctx: Context, bitmap: Bitmap, format: Bitmap.CompressFormat) =
    takeIf { it.isFile }?.let {
        ctx.contentResolver.openOutputStream(it.uri)?.use { os ->
            it.takeIf {
                withContext(Dispatchers.IO) { bitmap.compress(format, 100, os) }
            }
        }
    }

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    !is ContextWrapper -> null
    else -> baseContext.findActivity()
}

inline fun <reified T> Context.findOwner(): T? {
    var innerContext = this
    while (innerContext is ContextWrapper) {
        if (innerContext is T) return innerContext
        innerContext = innerContext.baseContext
    }
    return null
}

val DataUnit = mapOf(
    1f to "bit",
    8f to "Byte",
    1_000f to "Kb",
    1_024f to "Kib",
    8_000f to "KB",
    8_192f to "KiB", // = 8 * 1024
    1_000_000f to "Mb",
    1_048_576f to "Mib", // = 1024 * 1024
    8_000_000f to "MB",
    8_388_608f to "MiB", // = 8 * 1024 * 1024
)