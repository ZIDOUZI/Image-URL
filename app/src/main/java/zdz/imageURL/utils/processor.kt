package zdz.imageURL.utils

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import zdz.imageURL.model.data.Type


val idReg = Regex("(av|bv|cv|live|uid|pid|jm)[:：= ]*(\\d+)$", RegexOption.IGNORE_CASE)

fun Flow<String>.handleSpecialCases(): Flow<String> = map {
    when {
        it.startsWith("https://b23.tv/") -> Url(it).cleanURLParam().redirection()!!.cleanURLParam()
        it.startsWith("https://i.pximg.net/img-original/img") -> Url(it).pathSegments[8]
            .substringBefore('.')
            .let { id -> "https://www.pixiv.net/artworks/$id" }
        
        else -> it
    }
}

/**
 * @return [Type] to [Url]. Or null if [this] does not contain any legal url.
 */
suspend fun String.parseUrlString(): Pair<Type, String>? =
    urlReg.findAll(this).asFlow().map { it.value }
        .handleSpecialCases()
        .crossSingleOrNull(Type.entries) { url, type -> type.isUrlCompliance(url) }
        ?.let { (url, type) -> type to url }

/**
 * @see [single]
 * @see [first]
 */
fun String.parseHeadedIdString(): Pair<Type.Identifiable, String>? =
    (idReg.findAll(this).singleOrNull()?.value ?: this).let { source ->
        // to avoid the case that the string contains multiple id
        Type.identityObjects.firstNotNullOfOrNull { type ->
            type.verifyAndTrimHead(source)?.let { s -> type to type.url(s) }
        }
    }
