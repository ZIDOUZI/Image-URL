package zdz.bilicover

import java.net.URL

data class Data(
    val url: URL,
    val html_url: URL,
    val author: Author,
    val tag_name: String,
    val assets: Array<Assets>,
    val name: String,
) {
    fun isOutOfData(): Boolean {
        return name != "1.2.0"
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Data
        
        if (url != other.url) return false
        if (author != other.author) return false
        if (!assets.contentEquals(other.assets)) return false
        if (name != other.name) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + assets.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }
    
}

data class Author(
    val login: String,
)

data class Assets(
    val browser_download_url: URL,
)