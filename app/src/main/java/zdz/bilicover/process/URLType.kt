package zdz.bilicover.process

import java.net.URL
import kotlin.text.RegexOption.IGNORE_CASE

enum class URLType(
    val feature: Regex,
    val format: (String) -> String,
    val matches: (@URLString String) -> Boolean,
    val replacement: Regex,
) {
    BilibiliAV(
        Regex("av\\s*(\\d+)", IGNORE_CASE),
        { Regex("av(\\d+)", IGNORE_CASE).replace(it, "https://www.bilibili.com/video/av$1") },
        { Regex("https?://www\\.bilibili\\.com/video/AV(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("<meta data-vue-meta=\"true\" property=\"og:image\" content=\"(.*?)\\.(jpe?g|png)\">"),
    ),
    BilibiliBV(
        Regex("bv(\\w+)", IGNORE_CASE),
        { Regex("bv(\\w+)", IGNORE_CASE).replace(it, "https://www.bilibili.com/video/BV$1") },
        { Regex("https?://www\\.bilibili\\.com/video/BV(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("<meta data-vue-meta=\"true\" property=\"og:image\" content=\"(.*?)\\.(jpe?g|png)\""),
    ),
    BilibiliArticle(
        Regex("cv(\\d+)", IGNORE_CASE),
        { Regex("cv(\\d+)", IGNORE_CASE).replace(it, "https://www.bilibili.com/read/cv$1") },
        { Regex("https?://www\\.bilibili\\.com/read/CV(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("origin_img: \"(.*?)\",")
    ),
    BilibiliLive(
        TODO("尚未实现"),
        { TODO("尚未实现") },
        { Regex("https?://(api.)?live\\.bilibili\\.com/?(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("\"cover\":\"(.*?)\\.jpg\","),
    ),
    PixivUID(
        Regex("pid[:：]?\\s*(\\d)", IGNORE_CASE),
        { Regex("pid[:：]?\\s*(\\d)", IGNORE_CASE).replace(it, "https://www.pixiv.net/users/$1") },
        { Regex("https?://www\\.pixiv\\.net/users/\\d+", IGNORE_CASE).containsMatchIn(it) },
        Regex("\"original\":\"(.*?)\\.jpg\","),
    ),
    PixivPID(
        Regex("uid[:：]?\\s*(\\d)", IGNORE_CASE),
        { Regex("uid[:：]?\\s*(\\d)", IGNORE_CASE).replace(it, "https://www.pixiv.net/artworks/$1") },
        { Regex("https?://www\\.pixiv\\.net/artworks/\\d+", IGNORE_CASE).containsMatchIn(it) },
        Regex("\"original\":\"(.*?)\\.jpg\","),
    ),
    WechatArticle(
        TODO("尚未实现"),
        { TODO("尚未实现") },
        { Regex("https?://mp\\.weixin\\.qq\\.com/(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("msg_cdn_url = \"(.*?)jpe?g\";"),
    ),
    ;

    companion object {
        
        fun getURL(res: String) {
            if (urlReg.containsMatchIn(res)) {
                res.replace(urlReg, "$1")
            }
        }

        fun urlType(url: URL): URLType = urlType(url.toString())

        fun urlType(source: String): URLType = when {
            BilibiliAV.matches(source) -> BilibiliAV
            BilibiliBV.matches(source) -> BilibiliBV
            BilibiliArticle.matches(source) -> BilibiliArticle
            BilibiliLive.matches(source) -> BilibiliLive
            WechatArticle.matches(source) -> WechatArticle
            else -> throw NotImplementedError("尚未实现或不支持")
        }

    }

}