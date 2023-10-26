package zdz.imageURL

import org.junit.Test

class GlobalKtTest {
    @Test
    fun imgUrlFromUrl() {
        val sourceString = """\u6bb7\u767b\u8fa9\u683d\u7b50"""
        assert(String(sourceString.toByteArray(Charsets.UTF_8)) == "殷登辩栽筐")
    }
}