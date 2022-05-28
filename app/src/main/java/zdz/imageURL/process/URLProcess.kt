package zdz.imageURL.process

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

val urlReg =
    """((https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|])""".toRegex()

fun String.getURL(): URL? {
    val match = urlReg.find(this)
    return match?.let { URL(it.value) }
}

fun skipSSL() {
    try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        })
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    } catch (_: Exception) {
    }
}

fun URL.getSourceCode(encode: String? = "UTF-8"): String? {
    var contentBuffer: StringBuffer? = StringBuffer()
    val responseCode: Int
    val con: HttpURLConnection = try {
        openConnection() as HttpURLConnection
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    try {
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11") // IE代理进行下载
        con.connectTimeout = 60000
        con.readTimeout = 60000
        // 获得网页返回信息码
        responseCode = con.responseCode
        // 请求异常
        if (responseCode == -1) throw RuntimeException("$this : 连接失败...")
        // 请求失败
        if (responseCode >= 400) throw RuntimeException("请求失败.获取响应码: $responseCode")
        
        val inStr = con.inputStream
        val istreamReader = InputStreamReader(inStr, encode)
        val buffStr = BufferedReader(istreamReader)
        var str = ""
        while (buffStr.readLine()?.also { str = it } != null) contentBuffer!!.append(str)
        inStr.close()
    } catch (e: IOException) {
        e.printStackTrace()
        contentBuffer = null
    } finally {
        con.disconnect()
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