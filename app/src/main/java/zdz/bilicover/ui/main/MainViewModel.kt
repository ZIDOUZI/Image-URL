package zdz.bilicover.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import kotlin.coroutines.CoroutineContext

class MainViewModel : ViewModel() {
    //图片网址
    var url: URL? by mutableStateOf(null)
    
    //图片bitmap
    var bitmap: Bitmap? by mutableStateOf(null)
    
    //获得的永久使用uri
    var uri: Uri by mutableStateOf(Uri.parse(""))
    
    //uri的路径,而不是文件路径
    var path: File? by mutableStateOf(null)
    
    //缓存文件名
    var cacheName: String? by mutableStateOf(null)
    
    //文件路径
    var filePath: File? by mutableStateOf(null)
    
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
    
    val rootDir: DocumentFile? by mutableStateOf(null)
    
    fun launch(coroutineContext: CoroutineContext, coroutineScope: CoroutineScope.() -> Unit) =
        viewModelScope.launch { withContext(coroutineContext, coroutineScope) }
}