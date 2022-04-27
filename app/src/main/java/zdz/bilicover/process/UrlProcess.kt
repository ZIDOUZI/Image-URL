package zdz.bilicover.process

import zdz.bilicover.process.URLType.Companion.urlType
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.*

val urlReg = Regex("""((https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|])""")

fun String.getUrl(): URL? {
    val match = urlReg.find(this)
    return match?.let { URL(it.value) }
}

fun skipSSL() {
    try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            override fun checkClientTrusted(
                certs: Array<X509Certificate>,
                authType: String
            ) {
            }

            override fun checkServerTrusted(
                certs: Array<X509Certificate>,
                authType: String
            ) {
            }
        })

        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    } catch (_: Exception) {
    }
}

fun URL.getSourceCode(encode: String = "UTF-8"): String? {
    var contentBuffer: StringBuffer? = StringBuffer()
    val responseCode: Int
    var con: HttpURLConnection? = null
    try {
        con = openConnection() as HttpURLConnection
        con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)") // IE代理进行下载
        con.connectTimeout = 60000
        con.readTimeout = 60000
        // 获得网页返回信息码
        responseCode = con.responseCode
        if (responseCode == -1) {
            con.disconnect()
            throw Exception("请求失败, 连接不成功")
        }
        // 请求失败
        if (responseCode >= 400) {
            con.disconnect()
            throw Exception("请求失败, 响应代码$responseCode")
        }
        val inStr: InputStream = con.inputStream
        val iStreamReader = InputStreamReader(inStr, encode)
        val buffStr = BufferedReader(iStreamReader)
        var str: String?
        while (buffStr.readLine().also { str = it } != null) contentBuffer!!.append(str)
        inStr.close()
    } catch (e: IOException) {
        e.printStackTrace()
        contentBuffer = null
        println("error: $this")
    } finally {
        con?.disconnect()
    }
    return contentBuffer?.toString()
}

fun decodeUnicode16(str: String): String {
    val sb = StringBuilder()
    var i = 0
    while (i < str.length) {
        val c = str[i]
        if (c == '\\') {
            val c1 = str[i + 1]
            if (c1 == 'u') {
                val value = str.substring(i + 2, i + 6)
                sb.append(Integer.parseInt(value, 16).toChar())
                i += 5
            } else {
                sb.append(c1)
                i++
            }
        } else {
            sb.append(c)
        }
        i++
    }
    return sb.toString()
}

fun String.toHTTPS(): URL {
    return if (startsWith("http://")) {
        URL("https://" + substring(7))
    } else {
        URL(this)
    }
}

fun URL.getImgUrl(): URL {
    val sourceCode = getSourceCode()
    val type = sourceCode?.let { urlType(it) }
    checkNotNull(type) { "获取图片地址失败" }
    val r1 = type.replacement.find(sourceCode)?.value
    checkNotNull(r1) { "No replacement found" }
    val r2 = decodeUnicode16(r1)
    val r = urlReg.find(r2)?.value
    checkNotNull(r) { "No url found" }
    return r.toHTTPS()
}