package zdz.imageURL

import io.ktor.http.Url
import zdz.imageURL.Type.AV
import zdz.imageURL.Type.CV
import zdz.imageURL.Type.Companion.value
import zdz.imageURL.Type.Live
import zdz.imageURL.Type.PID
import zdz.imageURL.Type.UID
import zdz.imageURL.Type.WeChat
import zdz.libs.encode.softDecodeUnicode16
import zdz.libs.url.getSourceCode
import zdz.libs.url.toHTTPS
import zdz.libs.url.urlReg

/**
 * 要求[字符串][this]为符合规范的字符串,否则抛出错误
 * @throws[IllegalArgumentException]
 * [字符串][this]不符合规范
 * @throws[NotImplementedError]
 * [字符串][this]暂不支持
 */
fun String.idToUrl(): Url {
    require(idReg.findAll(this).toList().size == 1) { "不包含/包含多个id参数" }
    return idReg.replace(this) { "https://${value(it.groupValues[1]).domain}${it.groupValues[2]}" }.let(::Url)
}

val idReg = Regex("(av|bv|cv|live|uid|pid)[:：= ]*(\\d+)$", RegexOption.IGNORE_CASE)

/**
 * @throws[IllegalArgumentException][id]不符合
 * @throws[RuntimeException]未从源代码中找到匹配的图像链接 或 未能从解码后字符串中发现Url
 * @throws[NotImplementedError]不支持或尚未实现
 */
suspend fun imgUrlFromID(id: String): Url {
    require(idReg.findAll(id).toList().size == 1) { "不包含/包含多个id参数" }
    val imgUrl = idReg.find(id)!!.groupValues.let { l ->
        value(l[1]).run {
            if (this to name !in supportIDS) TODO("不支持或尚未实现")
            action(id.idToUrl().getSourceCode())
                ?: throw RuntimeException("获取${l[1]}${l[2]}源代码失败")
        }
    }
    val decoded = imgUrl.softDecodeUnicode16()
    val r = urlReg.find(decoded)?.value ?: throw RuntimeException("未能从解码后字符串中发现url")
    return r.toHTTPS()
}

suspend fun imgUrlFromUrl(url: Url): Url {
    val sourceCode = url.getSourceCode()
    val imgURL = AV.action(sourceCode)
        ?: CV.action(sourceCode)
        ?: Live.action(sourceCode)
        ?: WeChat.action(sourceCode) ?: TODO("未知类型或网址错误.\n$sourceCode")
    val decoded = imgURL.softDecodeUnicode16()
    val r = urlReg.find(decoded)?.value ?: throw RuntimeException("网页源代码中未找到图片url")
    return r.toHTTPS()
}

enum class Type {
    
    AV {
        override fun action(sourceCode: String): String? =
            """"og:image" content="([^">]+?\.(jpe?g|png))(@.+)?">""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
                ?.takeIf { sourceCode.contains("itemprop=\"video\"") }
                ?.let { "https:$it" }
        
        override val domain: String = "www.bilibili.com/video/av"
    },
    BV {
        override fun action(sourceCode: String): String? =
            """"og:image" content="([^">]+?\.(jpe?g|png))(@.+)?">""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
                ?.takeIf { sourceCode.contains("itemprop=\"video\"") }
                ?.let { "https:$it" }
        
        override val domain: String = "www.bilibili.com/video/bv"
    },
    CV {
        override fun action(sourceCode: String): String? =
            """"origin_image_urls": ?\["([^"\]]+?\.(jpe?g|png))"]""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }?.takeIf { sourceCode.contains("itemprop=\"Article\"") }
        
        override val domain: String = "www.bilibili.com/read/cv"
    },
    Live {
        override fun action(sourceCode: String): String? =
            """"cover":"(.+?\.jpg)",""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
        
        override val domain: String = "live.bilibili.com/"
    },
    WeChat {
        override fun action(sourceCode: String): String? =
            """msg_cdn_url = "(.+?jpe?g)";""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
        
        override val domain: String
            get() = TODO("Not yet implemented")
    },
    PID {
        override fun action(sourceCode: String): String? = null
        override val domain: String = "www.pixiv.net/artworks/"
    },
    UID {
        override fun action(sourceCode: String): String? = null
        override val domain: String = "www.pixiv.net/users/"
    },
    Unknown {
        override fun action(sourceCode: String): String? = null
        override val domain: String get() = TODO("Not yet implemented")
    };
    
    abstract fun action(sourceCode: String): String?
    abstract val domain: String
    
    companion object {
        fun value(value: String) = values().first { it.name.lowercase() == value.lowercase() }
    }
}

val supportIDS = listOf(AV, CV, Live, PID, UID).map { it to it.name } + (null to "禁用")