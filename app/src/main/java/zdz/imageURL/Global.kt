package zdz.imageURL

import zdz.imageURL.process.decodeUnicode16
import zdz.imageURL.process.getSourceCode
import zdz.imageURL.process.toHTTPS
import zdz.imageURL.process.urlReg
import java.net.URL

/**
 * 要求[id]为符合规范的字符串,否则抛出错误
 * @throws[IllegalArgumentException][id]不符合规范
 * @throws[NotImplementedError][id]暂不支持
 */
fun idToURL(id: String): URL {
    require(idReg.findAll(id).toList().size == 1) { "不包含/包含多个id参数" }
    val num = "\\d+".toRegex()
    return when {
        id.contains("av", true) -> URL("https://www.bilibili.com/video/av" + num.find(id)!!.value)
        id.contains("bv", true) -> URL("https://www.bilibili.com/video/BV" + num.find(id)!!.value)
        id.contains("cv", true) -> URL("https://www.bilibili.com/video/cv" + num.find(id)!!.value)
        id.contains("live", true) -> URL("https://live.bilibili.com/live/" + num.find(id)!!.value)
        id.contains("uid", true) -> URL("https://www.pixiv.net/users" + num.find(id)!!.value)
        id.contains("pid", true) -> URL("https://www.pixiv.net/artworks/" + num.find(id)!!.value)
        else -> TODO("不支持或尚未实现")
    }
}

val idReg = Regex("(av|bv|cv|live|uid|pid)[:：= ]*\\d+$", RegexOption.IGNORE_CASE)

/**
 * @throws[IllegalArgumentException][id]不符合
 * @throws[RuntimeException]未从源代码中找到匹配的图像链接 或 未能从解码后字符串中发现url
 * @throws[NotImplementedError]不支持或尚未实现
 */
fun imgURLFromId(id: String): URL {
    require(idReg.findAll(id).toList().size == 1) { "不包含/包含多个id参数" }
    val num = "\\d+".toRegex()
    val imgURL = num.find(id)!!.value.run {
        when {
            id.contains("av", true) -> URL("https://www.bilibili.com/video/av$this").getSourceCode()
                ?.let { """"og:image" content="(.*?)\.(jpe?g|png)">""".toRegex().find(it)?.value }
                ?: throw RuntimeException("获取av${this}源代码失败")
            id.contains("bv", true) -> URL("https://www.bilibili.com/video/BV$this").getSourceCode()
                ?.let { """"og:image" content="(.*?)\.(jpe?g|png)">""".toRegex().find(it)?.value }
                ?: throw RuntimeException("获取bv${this}源代码失败")
            id.contains("cv", true) -> URL("https://www.bilibili.com/video/cv$this").getSourceCode()
                ?.let { """origin_img: "(.*?)",""".toRegex().find(it)?.value }
                ?: throw RuntimeException("获取cv${this}源代码失败")
            id.contains("live", true) -> URL("https://live.bilibili.com/live/$this").getSourceCode()
                ?.let { """"cover":"(.*?)\.jpg",""".toRegex().find(it)?.value }
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
    val imgURL = """"og:image" content="(.*?)\.(jpe?g|png)">""".toRegex().find(sourceCode)?.value
        ?: """origin_img: "(.*?)",""".toRegex().find(sourceCode)?.value
        ?: """"cover":"(.*?)\.jpg",""".toRegex().find(sourceCode)?.value
        ?: """msg_cdn_url = "(.*?)jpe?g";""".toRegex().find(sourceCode)?.value ?: TODO("未知类型")
    val decoded = decodeUnicode16(imgURL)
    val r = urlReg.find(decoded)?.value ?: throw RuntimeException("网页源代码中未找到图片url")
    return r.toHTTPS()
}