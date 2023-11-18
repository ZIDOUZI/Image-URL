package zdz.imageURL.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import io.ktor.http.Url
import java.io.File

inline fun Intent(handler: Intent.() -> Unit): Intent = Intent().apply(handler)
inline fun Intent(action: String, handler: Intent.() -> Unit): Intent =
    Intent(action).apply(handler)


fun Context.viewContent(uri: Uri, mimeType: String? = null, choose: Boolean = true) =
    Intent(Intent.ACTION_VIEW) {
        setDataAndTypeAndNormalize(uri, mimeType)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }.let { if (choose) Intent.createChooser(it, "打开方式：") else it }.let { startActivity(it) }

fun Context.viewImage(image: File, choose: Boolean = true) =
    viewContent(image.toUri(), "image/*", choose)

fun Context.viewImage(image: Uri, choose: Boolean = true) =
    viewContent(image, "image/*", choose)

fun Context.viewUri(uri: Uri, choose: Boolean = true) = viewContent(uri, choose = choose)

fun Context.viewURL(url: Url, choose: Boolean = true) = viewUri(Uri.parse(url.toString()), choose)

inline fun Context.sendContent(choose: Boolean = true, putExtra: Intent.() -> Unit) =
    Intent(Intent.ACTION_SEND) {
        putExtra()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }.let { if (choose) Intent.createChooser(it, "分享到：") else it }.let { startActivity(it) }


fun Context.sendText(text: String, mimeType: String = "text/*", choose: Boolean = true) =
    sendContent(choose) {
        putExtra(Intent.EXTRA_TEXT, text).type = mimeType
    }

fun Context.sendImage(image: File, mimeType: String = "image/*", choose: Boolean = true) =
    sendImage(image.toUri(), mimeType, choose)

fun Context.sendImage(image: Uri, mimeType: String = "image/*", choose: Boolean = true) =
    sendContent(choose) {
        putExtra(Intent.EXTRA_STREAM, image)
        type = mimeType
    }

fun Context.sendURL(url: Url, choose: Boolean = true) = sendText(url.toString(), choose = choose)