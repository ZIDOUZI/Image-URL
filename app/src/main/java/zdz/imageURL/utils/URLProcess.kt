@file:Suppress("unused", "HttpUrlsUsage")

package zdz.imageURL.utils

import android.annotation.SuppressLint
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders.Location
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.Charsets
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


val urlReg = Regex("(https?|ftp|file)://[-\\w+&@#/%?=~|!:,.;]*[-\\w+&@#/%=~|]")
val urlNoParamReg = Regex("(https?|ftp|file)://[-\\w+&@#/%=~|!:,.;]*[-\\w+&@#/%=~|]")
val urlNoProtocolReg = Regex("[-\\w+&@#/%?=~|!:,.;]*[-\\w+&@#/%=~|]")

@SuppressLint("CustomX509TrustManager")
private val tm: X509TrustManager = object : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
}
val sslContext: SSLContext = SSLContext.getInstance("TLS").apply { init(null, arrayOf(tm), null) }

private val client = HttpClient(Android) {
    BrowserUserAgent()
}

/**
 * 获取网页源码
 * @param[fallbackEncode]网页源代码的编码格式
 * @return 网页源代码
 * @throws[Exception]请求失败,失败代码将在throw的信息中给出
 */
suspend fun Url.getSourceCode(
    skipSSL: Boolean = false,
    fallbackEncode: Charset = Charsets.UTF_8
): String {
    val response = HttpClient(Android) {
        BrowserUserAgent()
        engine {
            sslManager = {
                if (skipSSL) {
                    it.sslSocketFactory = sslContext.socketFactory
                    it.hostnameVerifier = HostnameVerifier { _, _ -> true }
                }
            }
        }
    }.get(this)
    
    when (response.status.value) {
        in 400..499 -> throw RuntimeException("请求失败.获取响应码: ${response.status.value}")
        -1 -> throw RuntimeException("$this : 连接失败...")
        else -> return response.bodyAsText(fallbackEncode)
    }
}

/**
 * 重定向.不适用于php重定向
 * @param[this@redirection]待重定型的网址
 * @return 重定向后的网址
 */
suspend fun Url.redirection(): String? =
    HttpClient(Android) { followRedirects = false }.get(this).headers[Location]


/**
 * 清理URL参数
 * @param[this@cleanURLParam]待清理参数的URL,
 * @return 清理参数后的URL
 * @throws[IllegalStateException]非空检查异常
 */
fun Url.cleanURLParam(): Url = URLBuilder(this).apply { parameters.clear() }.build()

/**
 * 将http网址转换为https网址
 */
fun Url.toHTTPS(): Url = URLBuilder(this).apply { protocol = URLProtocol.HTTPS }.build()

/**
 * 将http网址转换为https网址
 */
fun String.toHTTPS(): Url = URLBuilder(this).apply { protocol = URLProtocol.HTTPS }.build()