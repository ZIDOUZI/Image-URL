package zdz.imageURL.utils

val UTF_16 = Regex("""\\u([0-9a-fA-F]{4})""")

fun String.unescapeUnicode16() =
    replace(UTF_16) { it.groupValues[1].toInt(16).toChar().toString() }