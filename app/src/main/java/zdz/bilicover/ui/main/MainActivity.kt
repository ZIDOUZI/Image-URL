package zdz.bilicover.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zdz.bilicover.FileName
import zdz.bilicover.abPath
import zdz.bilicover.ui.NavItem
import zdz.bilicover.ui.theme.BilibiliCoverGetterTheme
import zdz.libs.url.getImgURL
import zdz.libs.url.getURL
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : ComponentActivity() {
    
    val vm: MainViewModel by viewModels()
    
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
                        composable(NavItem.GuideScr.route) { GuideScreen() }
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
        if (contentResolver.persistedUriPermissions.isNotEmpty()) {
            vm.path = contentResolver.persistedUriPermissions[0].uri.abPath
        }
        
    }
    
    /**
     * 处理链接,并将链接图片缓存到[bitmap][MainViewModel.bitmap]中.无论是通过分享还是粘贴解析都会先执行此函数
     * @param[res]通过分享或粘贴得到的原始文本
     * @return 处理后图片的链接
     */
    fun cache(res: String) {
        
        //删除上次解析产生的缓存,我才不会像某tx一样
        //最先执行,防止出错
        vm.cacheName?.let { deleteFile(it) }
        vm.cacheName = null
        
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
        intent.putExtra(EXTRA_STREAM, save())
        startActivity(Intent.createChooser(intent, "分享图片"))
    }
    
    fun save(path: File? = null, name: String? = "cacheFile"): File {
        
        val fileName = FileName(Regex("[a-z0-9]+\\.(png|jpe?g)").find(vm.url.toString())!!.value)
        val filePath: File
        val prefix = name ?: fileName.prefix
        val suffix = fileName.suffix
        
        
        //判断图片格式
        val format = when (suffix) {
            ".jpg", ".jpeg" -> Bitmap.CompressFormat.JPEG
            ".png" -> Bitmap.CompressFormat.PNG
            ".webp" -> Bitmap.CompressFormat.WEBP
            else -> throw IllegalStateException("MainActivity: save 未知的图片格式")
        }
        
        filePath = if (path == null) {
            
            //传出fileName保证再次解析链接时能删除上次留下的链接
            vm.cacheName = "$prefix$suffix"
            
            File(externalCacheDir, "$prefix$suffix")
        } else {
            //保存本地后文件不再归于应用管理,不保留文件名
            check(vm.cacheName != null) { "文件名不为null" }
            File("$path/${vm.cacheName}")
        }
        
        //IO线程中写入文件
        vm.launch(Dispatchers.IO) {
            if (filePath.exists()) {
                filePath.delete()
            }
            val fileOutputStream = FileOutputStream(filePath)
            vm.bitmap?.compress(format, 100, fileOutputStream)
            fileOutputStream.close()
        }
        return filePath
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
    
    override fun onDestroy() {
        super.onDestroy()
        vm.cacheName?.let { File("$cacheDir/$it").delete() }
        //TODO: 清除缓存
    }
}