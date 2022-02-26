package zdz.bilicover

data class FileName(val prefix: String, val suffix: String) {
    
    constructor(name: String) : this(
        name.substring(0 until name.lastIndexOf('.')),
        name.substring((name.lastIndexOf('.')))
    )
    
    val name: String
        get() = "$prefix$suffix"
    
    fun rename(prefix: String = this.prefix, suffix: String = this.suffix) = FileName(prefix, suffix)
}