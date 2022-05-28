package zdz.imageURL.ui.main

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import zdz.imageURL.idReg
import zdz.imageURL.pref.core.Prefs
import zdz.imageURL.process.urlReg
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val prefs: Prefs,
) : ViewModel() {
    
    companion object {
        const val version = "2.0.0"
    }
    
    val debug: Boolean = try {
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    } catch (e: Exception) {
        false
    }
    
    /** 图片网址 */
    var url: URL? by mutableStateOf(null)
    
    /** 图片bitmap */
    var bitmap: Bitmap? by mutableStateOf(null)
    
    /** 保存文件的根目录 */
    var rootDir: DocumentFile? by mutableStateOf(null)
    
    /** 输入框文本 */
    var text by mutableStateOf("")
    
    /**
     * 输入框状态
     */
    val error get() = !text.contains(urlReg) && idReg.findAll(text).toList().size != 1
    
    /**
     * 待销毁的文件
     */
    var destroy: MutableMap<Uri, Boolean> = mutableStateMapOf()
    
    var logs by mutableStateOf("")
    
    val dirContracts = object :
        ActivityResultContracts.OpenDocumentTree() {
        override fun createIntent(context: Context, input: Uri?): Intent {
            return super.createIntent(context, input).apply {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }
    
    var darkTheme: Boolean? by mutableStateOf(prefs.theme)
    var transparent by mutableStateOf(prefs.transparent)
}