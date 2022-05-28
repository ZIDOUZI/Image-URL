package zdz.imageURL

import zdz.imageURL.Type.*
import zdz.imageURL.process.decodeUnicode16
import zdz.imageURL.process.getSourceCode
import zdz.imageURL.process.toHTTPS
import zdz.imageURL.process.urlReg
import java.net.URL

/**
 * 要求[字符串][this]为符合规范的字符串,否则抛出错误
 * @throws[IllegalArgumentException]
 * [字符串][this]不符合规范
 * @throws[NotImplementedError]
 * [字符串][this]暂不支持
 */
fun String.idToURL(): URL {
    require(idReg.findAll(this).toList().size == 1) { "不包含/包含多个id参数" }
    val num = "\\d+".toRegex()
    return when {
        contains("av", true) -> URL("https://www.bilibili.com/video/av" + num.find(this)!!.value)
        contains("bv", true) -> URL("https://www.bilibili.com/video/BV" + num.find(this)!!.value)
        contains("cv", true) -> URL("https://www.bilibili.com/video/cv" + num.find(this)!!.value)
        contains("live", true) -> URL("https://live.bilibili.com/live/" + num.find(this)!!.value)
        contains("uid", true) -> URL("https://www.pixiv.net/users/" + num.find(this)!!.value)
        contains("pid", true) -> URL("https://www.pixiv.net/artworks/" + num.find(this)!!.value)
        else -> TODO("不支持或尚未实现")
    }
}

val idReg = Regex("(av|bv|cv|live|uid|pid)[:：= ]*\\d+$", RegexOption.IGNORE_CASE)

val numReg = Regex("\\d+")

/**
 * @throws[IllegalArgumentException][id]不符合
 * @throws[RuntimeException]未从源代码中找到匹配的图像链接 或 未能从解码后字符串中发现url
 * @throws[NotImplementedError]不支持或尚未实现
 */
fun imgURLFromId(id: String): URL {
    require(idReg.findAll(id).toList().size == 1) { "不包含/包含多个id参数" }
    val imgURL = numReg.find(id)!!.value.run {
        when {
            id.contains("av", true) -> URL("https://www.bilibili.com/video/av$this").getSourceCode()
                ?.let { AV.action(it) }
                ?: throw RuntimeException("获取av${this}源代码失败")
            id.contains("bv", true) -> URL("https://www.bilibili.com/video/BV$this").getSourceCode()
                ?.let { AV.action((it)) }
                ?: throw RuntimeException("获取bv${this}源代码失败")
            id.contains("cv", true) -> URL("https://www.bilibili.com/video/cv$this").getSourceCode()
                ?.let { CV.action(it) }
                ?: throw RuntimeException("获取cv${this}源代码失败")
            id.contains("live", true) ->
                URL(null, "https://live.bilibili.com/live/$this").getSourceCode()
                    ?.let { Live.action(it) }
                    ?: throw RuntimeException("获取live${this}源代码失败")
            id.contains("pid", true) -> URL("https://www.pixiv.net/artworks/$this").getSourceCode()
                ?.let { """<img src="(.*?)" alt="""".toRegex().find(it)?.value }
                ?: throw RuntimeException("获取pid${this}源代码失败")
            id.contains("uid", true) -> throw IllegalArgumentException("uid不支持获取图像")
            else -> TODO("不支持或尚未实现")
        }
    }
    val decoded = decodeUnicode16(imgURL)
    val r = urlReg.find(decoded)?.value ?: throw RuntimeException("未能从解码后字符串中发现url")
    return r.toHTTPS()
}

fun imgURLFromURL(url: URL): URL {
    val sourceCode = url.getSourceCode()
    checkNotNull(sourceCode) { "未获取到网页源代码" }
    val imgURL = AV.action(sourceCode)
        ?: CV.action(sourceCode)
        ?: Live.action(sourceCode)
        ?: WeChat.action(sourceCode) ?: TODO("未知类型")
    val decoded = decodeUnicode16(imgURL)
    val r = urlReg.find(decoded)?.value ?: throw RuntimeException("网页源代码中未找到图片url")
    return r.toHTTPS()
}

enum class Type {
    
    AV {
        override fun action(sourceCode: String): String? =
            """"og:image" content="([^">]+?\.(jpe?g|png))">""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
    },
    BV {
        override fun action(sourceCode: String): String? =
            """"og:image" content="([^">]+?\.(jpe?g|png))">""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
    },
    CV {
        override fun action(sourceCode: String): String? =
            """origin_img: "([^"\]]+?\.(jpe?g|png))"""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
    },
    Live {
        override fun action(sourceCode: String): String? =
            """"cover":"(.+?\.jpg)",""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
    },
    WeChat {
        override fun action(sourceCode: String): String? =
            """msg_cdn_url = "(.+?jpe?g)";""".toRegex().find(sourceCode)
                ?.run { groupValues[1] }
    },
    PID {
        override fun action(sourceCode: String): String? = null
    },
    UID {
        override fun action(sourceCode: String): String? = null
    },
    Unknown {
        override fun action(sourceCode: String): String? = null
    };
    
    abstract fun action(sourceCode: String): String?
    
}

val supportIDS = listOf(AV, CV, Live, PID, UID).map { it to it.name } + (null to "禁用")