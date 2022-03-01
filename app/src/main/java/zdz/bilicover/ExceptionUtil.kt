package zdz.bilicover

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExceptionUtil(private val throwIt: Boolean = true) : Exception() {
    
    private fun currentTimeFormatted(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    
    override var message: String = currentTimeFormatted()
    
    constructor(message: String, throwIt: Boolean) : this(throwIt) { this.message += message }
    
    fun throwException() {
        if (throwIt) {
            throw cause ?: Exception()
        }
    }
}