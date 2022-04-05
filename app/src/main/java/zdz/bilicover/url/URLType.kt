package zdz.bilicover.url

import java.net.URL
import kotlin.text.RegexOption.IGNORE_CASE

enum class URLType(
    val matches: (@URLString String) -> Boolean,
    val replacement: Regex,
) {
    VideoAV(
        { Regex("https?://www\\.bilibili\\.com/video/AV(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("<meta data-vue-meta=\"true\" property=\"og:image\" content=\"(.*?)\\.(jpe?g|png)\">"),
    ),
    VideoBV(
        { Regex("https?://www\\.bilibili\\.com/video/BV(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("<meta data-vue-meta=\"true\" property=\"og:image\" content=\"(.*?)\\.(jpe?g|png)\""),
    ),
    Article(
        { Regex("https?://www\\.bilibili\\.com/read/CV(.*)", IGNORE_CASE).containsMatchIn(it) },
//        Regex("\"origin_image_urls\":\\[\"(.*?)\"[,\\]]"),//TODO: 由于某些原因,IE代理的源代码只有origin_img格式的网址
        Regex("origin_img: \"(.*?)\",")
    ),
    Live(
        { Regex("https?://(api.)?live\\.bilibili\\.com/?(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("\"cover\":\"(.*?)\\.jpg\","),
    ),
    WechatArticle(
        { Regex("https?://mp\\.weixin\\.qq\\.com/(.*)", IGNORE_CASE).containsMatchIn(it) },
        Regex("msg_cdn_url = \"(.*?)jpe?g\";"),
    ),
    ;

    companion object {

        fun getType(url: URL): URLType = getType(url.toString())

        fun getType(source: String): URLType = when {
            VideoAV.matches(source) -> VideoAV
            VideoBV.matches(source) -> VideoBV
            Article.matches(source) -> Article
            Live.matches(source) -> Live
            WechatArticle.matches(source) -> WechatArticle
            else -> throw NotImplementedError("尚未实现或不支持")
        }

    }

}