package zdz.imageURL.activity.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.*
import kotlinx.coroutines.Job
import zdz.imageURL.*
import zdz.libs.compose.pref.core.core.PrefMaker
import zdz.libs.compose.pref.core.nullableStringSerializer
import zdz.libs.url.urlReg
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {
    
    companion object {
        const val version = "2.1.0"
    }
    
    inline fun <T> stateBy(crossinline block: () -> T): State<T> = object : State<T> {
        override val value: T
            get() = block()
    }
    private val maker = PrefMaker(context.getSharedPreferences("prefs", Context.MODE_PRIVATE))
    
    val darkTheme = maker.any(
        R.string.dark_theme,
        null,
        nullableStringSerializer({ it?.toString() }, { it?.toBooleanStrict() })
    )
    val transparent = maker.bool(R.string.transparent)
    val alpha = maker.float(R.string.opacity)
    val firstLink = maker.bool(R.string.first_link)
    val secondLink = maker.bool(R.string.second_link, true)
    val autoCheck = maker.bool(R.string.auto_check, !BuildConfig.DEBUG)
    val advanced = maker.bool(R.string.advanced)
    val preferredID = maker.enum<Type>(R.string.preferred_id)
    val autoJump = maker.bool(R.string.auto_jump, true)
    val closeAfterProcess = maker.bool(R.string.close_after_process)
    
    /** 源url */
    var sourceURL: Url? by mutableStateOf(null)
    
    /** 图片网址 */
    var imgUrl: Url? by mutableStateOf(null)
    
    /** 图片bitmap */
    var bitmap: Bitmap? by mutableStateOf(null)
    
    /** 保存文件的根目录 */
    var rootDir: DocumentFile? by mutableStateOf(null)
    
    /** 输入框文本 */
    var text by mutableStateOf("")
    
    /** 输入框状态 */
    val error by stateBy {
        !text.contains(urlReg)
                && (preferredID.state != Type.Unknown && !text.matches(numReg))
                && idReg.findAll(text).toList().size != 1
    }
    
    /**
     * 日志信息
     */
    var logs: AnnotatedString by mutableStateOf(AnnotatedString(""))
    
    /**
     * AlertDialog显示状态
     */
    var show by autoCheck.toMutableState()
    
    var job: Job? = null
    
    val dirContracts = object : ActivityResultContracts.OpenDocumentTree() {
        override fun createIntent(context: Context, input: Uri?): Intent {
            return super.createIntent(context, input).apply {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }
}