package zdz.bilicover.ui.main

import android.content.*
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zdz.bilicover.ui.NavItem
import zdz.bilicover.ui.theme.BilibiliCoverGetterTheme
import zdz.libs.url.getImgURL
import zdz.libs.url.getURL
import java.io.InputStream

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val vm: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BilibiliCoverGetterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = NavItem.MainScr.route
                    ) {
                        composable(NavItem.MainScr.route) {
                            MainScreen(
                                navController,
                                vm,
                                this@MainActivity
                            )
                        }
                        composable(NavItem.GuideScr.route) { GuideScreen(vm.debug) }
                    }
                }
            }
        }
        
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    intent.getStringExtra(EXTRA_TEXT)?.let {
                        cache(it)
                    }
                    toast("获取分享成功")
                }
            }
        }
        
        //初始化时将保有的永久uri访问地址读取到viewModel
        setRoot()
        
    }
    
    /**
     * 处理链接,并将链接图片缓存到[bitmap][MainViewModel.bitmap]中.无论是通过分享还是粘贴解析都会先执行此函数
     * @param[res]通过分享或粘贴得到的原始文本
     * @return 处理后图片的链接
     */
    fun cache(res: String) {
        
        vm.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                vm.url = getImgURL(getURL(res)).also {
                    val inputStream: InputStream = it.openStream()
                    vm.bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                }
            }
        }
        toast("解析成功")
    }
    
    fun shareImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        
        val uri = FileProvider.getUriForFile(this, "zdz.bilicover.fileProvider", save().toFile())
        
        intent.putExtra(EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "分享图片"))
        
    }
    
    /**
     * @param[name]文件名.默认为cacheFile.使用cacheFile作为文件名将保存在缓存目录,如果要使用下载时的原名请设为null
     */
    fun save(name: String? = "cacheFile"): Uri {
        
        //从网络获取的原图片名
        val sourceName = Regex("[a-z0-9]+\\.(png|jpe?g)").find(vm.url.toString())!!.value
        val prefix = name ?: sourceName.substring(0 until sourceName.lastIndexOf('.'))
        val suffix = sourceName.substring(sourceName.lastIndexOf('.'))
        val fileName = prefix + suffix
        
        //判断图片格式
        val format: Bitmap.CompressFormat
        val mimeType: String
        
        when (suffix) {
            ".jpg", ".jpeg" -> {
                mimeType = "*/jpg"
                format = Bitmap.CompressFormat.JPEG
            }
            ".png" -> {
                mimeType = "*/png"
                format = Bitmap.CompressFormat.PNG
            }
            ".webp" -> {
                mimeType = "*/webp"
                format = if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSLESS
                else Bitmap.CompressFormat.WEBP
            }
            else -> throw IllegalStateException("MainActivity: save 未知的图片格式")
        }
        
        
        //TODO: 适配content:///格式uri
        
        val folder = if (prefix == "cacheFile") {
            externalCacheDir?.let { DocumentFile.fromFile(it) }
        } else {
            vm.rootDir
        } ?: throw Exception("????????")
        
        val resolve = folder.findFile(prefix)?.takeIf { !it.isDirectory }
            ?: folder.createFile(mimeType, fileName)
            ?: throw Exception("........")
        
        vm.launch(Dispatchers.IO) {
            val outputStream = contentResolver.openOutputStream(resolve.uri)
            if (vm.bitmap?.compress(format, 100, outputStream) == true) toast("保存成功")
            outputStream?.close()
        }
        
        return resolve.uri
    }
    
    /**
     * 复制到剪贴板
     */
    fun copy() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // 创建普通字符型ClipData
        val mClipData =
            ClipData.newPlainText(
                "copy image link",
                vm.url.toString()
            )
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData)
        toast("复制成功")
    }
    
    /**
     * 分享链接
     */
    fun shareURL() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/*"
        intent.putExtra(EXTRA_TEXT, vm.url)
        startActivity(Intent.createChooser(intent, "分享到："))
    }
    
    fun toast(text: String) = Toast.makeText(this, text, LENGTH_SHORT).show()
    
    fun setRoot() {
        if (contentResolver.persistedUriPermissions.isNotEmpty()) {
            vm.rootDir = DocumentFile.fromTreeUri(
                this,
                contentResolver.persistedUriPermissions.takeIf { it.isNotEmpty<UriPermission?>() }
                    ?.get(0)?.uri
                    ?: throw Exception(">>>>>>>>>>>>>>>>>>>"/* TODO() */)
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        //清除缓存
        vm.rootDir?.findFile("cacheFile")?.delete()
    }
    
}