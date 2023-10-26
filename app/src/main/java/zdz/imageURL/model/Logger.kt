package zdz.imageURL.model

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Singleton
class Logger @Inject constructor() {
    private val dataFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val _logs = mutableStateListOf<AnnotatedString>()
    fun getLog() = _logs.fold(AnnotatedString("")) { acc, s -> acc + s }
    
    private enum class Level(val levelStyle: SpanStyle, val textStyle: SpanStyle) {
        VERBOSE(
            SpanStyle(color = Color.White, background = Color.Black, fontWeight = FontWeight.W600),
            SpanStyle(color = Color.Gray)
        ),
        INFO(
            SpanStyle(color = Color.White, background = Color.Green, fontWeight = FontWeight.W600),
            SpanStyle(color = Color.Green)
        ),
        DEBUG(
            SpanStyle(color = Color.White, background = Color.Blue, fontWeight = FontWeight.W600),
            SpanStyle(color = Color.LightGray)
        ),
        WARNING(
            SpanStyle(color = Color.Black, background = Color.Yellow, fontWeight = FontWeight.W600),
            SpanStyle(color = Color.Yellow)
        ),
        ERROR(
            SpanStyle(color = Color.Black, background = Color.Red, fontWeight = FontWeight.W600),
            SpanStyle(color = Color.Red)
        ),
        ;
        
        val priority: Int = ordinal + 2
        val levelText = " ${name[0]} "
    }
    
    private fun log(
        s: String,
        level: Level? = null,
        color: Color? = null,
        th: Throwable? = null
    ) {
        Log.println(
            level?.priority ?: Log.VERBOSE,
            "App-Logger",
            s + Log.getStackTraceString(th)
        )
        _logs += AnnotatedString.Builder().apply {
            level?.let {
                withStyle(timeStyle) {
                    append("[", dataFormat.format(Date()), "]")
                }
                append(" ")
                withStyle(level.levelStyle) {
                    append(level.levelText)
                }
                append(" ")
            }
            withStyle(level?.textStyle ?: color?.let { SpanStyle(it) } ?: defaultStyle) {
                append(s, "\n")
            }
        }.toAnnotatedString()
    }
    
    private val timeStyle =
        SpanStyle(color = Color.Black, background = Color.LightGray, fontWeight = FontWeight.W600)
    
    private val defaultStyle =
        SpanStyle(color = Color.Black, background = Color.White, fontWeight = FontWeight.W600)
    
    fun v(s: String) = log(s, Level.VERBOSE)
    fun i(s: String) = log(s, Level.INFO)
    fun d(s: String) = log(s, Level.DEBUG)
    fun w(s: String, th: Throwable? = null) = log(s, Level.WARNING, th = th)
    fun w(th: Throwable) = w("", th)
    fun e(s: String, th: Throwable? = null) = log(s, Level.ERROR, th = th)
    fun e(th: Throwable) = e("", th)
    fun append(s: String, color: Color = Color.Black) = log(s, color = color)
    
    @OptIn(ExperimentalContracts::class)
    inline fun <T> measureTimeMillis(pattern: String, block: () -> T): T {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val start = System.currentTimeMillis()
        val r = block()
        i(pattern.format(System.currentTimeMillis() - start))
        return r
    }
}