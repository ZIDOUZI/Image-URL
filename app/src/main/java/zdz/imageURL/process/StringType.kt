package zdz.imageURL.process

enum class StringType(
    val matches: (String) -> Boolean,
    val format: (String) -> String,
) {
    
    UnknownID(
        { Regex("[0-9]{9,}").containsMatchIn(it) },
        { throw IllegalArgumentException("Unknown ID: $it") }
    ),
    
    ;
    companion object {
        fun stringType(string: String): StringType {
            return values().find { it.matches(string) } ?: UnknownID
        }
    }
}