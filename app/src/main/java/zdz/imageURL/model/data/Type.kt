package zdz.imageURL.model.data

import zdz.imageURL.utils.idReg
import zdz.imageURL.utils.toHTTPS
import zdz.imageURL.utils.unescapeUnicode16
import zdz.libs.preferences.contracts.Serializer

sealed interface Type {
    
    companion object {
        val identityEntries = listOf(AV, CV, MobileCV, Live, PID, UID)
        val identityObjects = identityEntries + JM
        val entries = listOf(AV, BV, CV, MobileCV, Live, WeChat, PID, UID)
        
        object TypeSerializer : Serializer<String?, Identifiable?> {
            override fun deserialize(t: Identifiable?): String? = t?.name
            override fun serialize(s: String?): Identifiable? =
                identityObjects.firstOrNull { it.name == s }
        }
    }
    
    val domain: String
    fun isUrlCompliance(url: String): Boolean = url.substring(8).startsWith(domain)
    
    /**
     * 拥有id的类型. 可以通过id获得对应的URL
     */
    sealed interface Identifiable : Type {
        val name: String
        
        fun verifyAndTrimHead(idString: String): String? =
            idString.takeIf { it.startsWith(name, true) }?.let {
                idReg.find(it)?.groupValues?.get(2)
            }
        
        fun url(id: String): String = "https://$domain$id"
    }
    
    /**
     * 可以从源代码中提取图片链接的类型
     */
    sealed interface Extractable : Type {
        fun extractImageUrl(sourceCode: String): String
    }
    
    data object AV : Extractable, Identifiable {
        override val domain: String = "www.bilibili.com/video/av"
        override val name: String = "AV"
        private val detectReg = """"og:image" content="([^">]+?\.(jpe?g|png))(@.+)?">""".toRegex()
        override fun extractImageUrl(sourceCode: String): String =
            detectReg.find(sourceCode)?.run { groupValues[1] }
                ?.takeIf { sourceCode.contains("itemprop=\"video\"") }?.let { "https:$it" }
                ?: throw RuntimeException("源代码已改变")
    }
    
    data object BV : Extractable, Identifiable {
        override val domain: String = "www.bilibili.com/video/BV"
        override val name: String = "BV"
        private val bvReg = Regex("BV(\\w{10})")
        override fun verifyAndTrimHead(idString: String): String? =
            bvReg.find(idString)?.groupValues?.get(1)
        
        private val detectReg = """"og:image" content="([^">]+?\.(jpe?g|png))(@.+)?">""".toRegex()
        override fun extractImageUrl(sourceCode: String): String =
            detectReg.find(sourceCode)?.run { groupValues[1] }
                ?.takeIf { sourceCode.contains("itemprop=\"video\"") }?.let { "https:$it" }
                ?: throw RuntimeException("源代码已改变")
    }
    
    data object CV : Extractable, Identifiable {
        override val domain: String = "www.bilibili.com/read/cv"
        override val name: String = "CV"
        
        private val detectReg = """"origin_image_urls": ?\["([^"\]]+?\.(jpe?g|png))"]""".toRegex()
        override fun extractImageUrl(sourceCode: String): String =
            detectReg.find(sourceCode)?.run { groupValues[1].unescapeUnicode16() }
                ?: throw RuntimeException("源代码已改变")
    }
    
    /**
     * 2023-11-18, before that time, bilibili changed redirection to `/read/mobile/cv`
     */
    data object MobileCV : Extractable, Identifiable {
        override val domain: String = "www.bilibili.com/read/mobile/"
        override val name: String = "CV"
        
        override fun url(id: String): String = CV.url(id)
        private val detectReg = """"origin_image_urls": ?\["([^"\]]+?\.(jpe?g|png))"]""".toRegex()
        override fun extractImageUrl(sourceCode: String): String =
            detectReg.find(sourceCode)?.run { groupValues[1].unescapeUnicode16() }
                ?: throw RuntimeException("源代码已改变")
    }
    
    data object Live : Extractable, Identifiable {
        override val domain: String = "live.bilibili.com"
        override val name: String = "Live"
        private val detectReg = """"cover":"(.+?\.jpg)",""".toRegex()
        override fun extractImageUrl(sourceCode: String): String =
            detectReg.find(sourceCode)?.run { groupValues[1].unescapeUnicode16() }?.toHTTPS()
                ?: throw RuntimeException("源代码已改变")
    }
    
    data object WeChat : Extractable {
        override val domain: String = "mp.weixin.qq.com"
        private val detectReg = """msg_cdn_url = "(.+?jpe?g)";""".toRegex()
        override fun extractImageUrl(sourceCode: String): String =
            detectReg.find(sourceCode)?.run { groupValues[1] }
                ?: throw RuntimeException("源代码已改变")
    }
    
    data object PID : Identifiable {
        override val domain: String = "www.pixiv.net/artworks/"
        override val name: String = "PID"
        override fun verifyAndTrimHead(idString: String) = super.verifyAndTrimHead(idString)
                ?: idString.filter { it.isDigit() }.takeIf { it.length == 9 }
    }
    
    data object UID : Identifiable {
        override val domain: String = "www.pixiv.net/users/"
        override val name: String = "UID"
    }
    
    data object JM : Identifiable {
        val mirrorSites = listOf(
            "18comic.vip/album/",
            "18comic.org/album/",
            "jmcomic.me/album/",
            "jmcomic1.me/album/",
            "18comic-god.cc/album/",
            "18comic-god.club/album/",
            "18comic-god.xyz/album/",
        )
        override var domain: String = mirrorSites.first()
        override val name: String = "JM"
        override fun verifyAndTrimHead(idString: String) = super.verifyAndTrimHead(idString)
            ?: idString.takeIf { it.startsWith("晋M") }?.substring(2, 8)
            ?: idString.filter { it.isDigit() }.takeIf { it.length == 6 }
            ?: idString.filter { it in SIMPLIFIED }.takeIf { it.length == 6 }
                ?.fold("") { acc, c -> acc + SIMPLIFIED.indexOf(c).toString() }
            ?: idString.filter { it in TRADITIONAL }.takeIf { it.length == 6 }
                ?.fold("") { acc, c -> acc + TRADITIONAL.indexOf(c).toString() }
        
        private const val SIMPLIFIED = "零一二三四五六七八九"
        private const val TRADITIONAL = "零壹貳參肆伍陸柒捌玖"
    }
    
    data object Unknown : Type {
        override val domain: String = throw IllegalStateException("Can not get domain for a unknown type.")
    }
}