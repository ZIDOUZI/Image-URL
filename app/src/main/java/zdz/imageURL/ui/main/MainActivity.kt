package zdz.imageURL.ui.main

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import zdz.imageURL.Data
import zdz.imageURL.R
import zdz.imageURL.imgURLFromId
import zdz.imageURL.imgURLFromURL
import zdz.imageURL.process.getSourceCode
import zdz.imageURL.process.getURL
import zdz.imageURL.process.skipSSL
import zdz.imageURL.ui.NavItem
import zdz.imageURL.ui.main.sub.GuideScreen
import zdz.imageURL.ui.main.sub.LogScreen
import zdz.imageURL.ui.main.sub.MainScreen
import zdz.imageURL.ui.main.sub.SettingsScreen
import zdz.imageURL.ui.theme.BilibiliCoverGetterTheme
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.URL


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val vm: MainViewModel by viewModels()
    
    lateinit var pickDir: ActivityResultLauncher<Uri?>
    
    lateinit var data: Data
    
    private val scope by lazy { vm.viewModelScope }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BilibiliCoverGetterTheme(
                darkTheme = vm.darkTheme,
                transparent = vm.transparent,
            ) {
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        NavigationBar {
                            NavItem.values().forEach {
                                NavigationBarItem(
                                    selected = currentRoute == it.route,
                                    onClick = { navController.navigate(it.route) },
                                    alwaysShowLabel = false,
                                    icon = {
                                        Icon(
                                            painter = painterResource(it.icon),
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = it.route
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(id = it.string),
                                            fontSize = 11.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) {
                    NavHost(
                        modifier = Modifier.padding(it),
                        navController = navController,
                        startDestination = NavItem.MainScr.route
                    ) {
                        composable(NavItem.MainScr.route) {
                            MainScreen(navController, vm, this@MainActivity)
                        }
                        composable(NavItem.HelpScr.route) { GuideScreen(vm.debug) }
                        composable(NavItem.SettingsScr.route) {
                            SettingsScreen(vm, this@MainActivity)
                        }
                        composable(NavItem.LogScr.route) {
                            LogScreen(vm)
                        }
                    }
                }
            }
        }
        
        //启动时检测意图
        if (intent?.action == Intent.ACTION_SEND) {
            when (intent.type) {
                "text/plain" ->
                    intent.getStringExtra(EXTRA_TEXT)?.let {
                        vm.text = it
                        process()
                    } ?: toast("分享错误.分享内容为空")
            }
        }
        
        //初始化时将保有的永久uri访问地址读取到viewModel
        setRoot()
        
        // TODO: 尝试使用延迟属性
        pickDir = registerForActivityResult(vm.dirContracts) {
            if (it != null) {
                if (vm.rootDir?.uri != it && vm.rootDir != null) {
                    try {
                        contentResolver.releasePersistableUriPermission(
                            vm.rootDir!!.uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast("释放可持久 URI 时出错")
                    }
                }
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                vm.rootDir = DocumentFile.fromTreeUri(this, it)
            }
        }
        
        scope.launch(Dispatchers.IO) {
            skipSSL()
            try {
                data = Json.decodeFromString(URL(getString(R.string.update_url)).getSourceCode()!!)
//                if (data.isOutOfData(MainViewModel.version)) toast("检测到可用更新")
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                Log.e("$e", "Error on getting latest release")
            }
        }
        // TODO: 弹窗更新
    }
    
    
    override fun onDestroy() {
        super.onDestroy()
        //清除缓存
        vm.destroy.filter { (_, v) -> v }.keys.forEach {
            DocumentFile.fromTreeUri(this, it)?.delete()
        }
        externalCacheDir?.let { DocumentFile.fromFile(it) }?.findFile("cacheFile")?.delete()
    }
    
    /**
     * 处理输入框/分享文本.缓存图片链接到[bitmap][MainViewModel.bitmap]中.由于MainScreen中的图片使用
     * [bitmap][MainViewModel.bitmap]加载,因此缓存完成后会立即显示图像
     */
    fun process(res: String? = null) {
        val s = res ?: vm.text
        scope.launch(Dispatchers.IO) {
            try {
                vm.url = s.getURL()?.let { imgURLFromURL(it) } ?: imgURLFromId(s)
                cache(vm.url!!)
            } catch (e: Throwable) {
                e.printStackTrace()
                toast(e.message ?: e.toString())
            }
        }
    }
    
    /**
     * 缓存图片链接到[bitmap][MainViewModel.bitmap]中.由于MainScreen中的图片使用
     * [bitmap][MainViewModel.bitmap]加载,因此缓存完成后会立即显示图像
     * 注意:必须在[CoroutineScope]中运行
     * @param[url]图片的url
     * @throws[]
     */
    private fun cache(url: URL) {
        try {
            val inputStream: InputStream = url.openStream()
            vm.bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            toast(e.message ?: e.toString())
        }
        // TODO: okhttp导致请求无法正常完成
    }
    
    /**
     *  调用系统下载器下载文件.默认下载完成后打开文件
     */
    fun download(url: String, open: Boolean = true) {
        val fileName = "${getString(R.string.app_name)}_release_${data.tagName}.apk"
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("下载中")
            setDescription("正在下载$fileName")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = manager.enqueue(request)
        scope.launch(Dispatchers.IO) {
            while (manager.getMimeTypeForDownloadedFile(id) == null) {
                delay(3000)
            }
            if (open) manager.openDownloadedFile(id)
        }
    }
    
    /**
     * 分享图片
     */
    fun shareImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        
        // 防止uri泄露.如不使用provider重建则导致异常
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
        val sourceName = Regex("\\w+\\.(png|jpe?g)").find(vm.url.toString())?.value
            ?: Regex(".+mmbiz_jpg/(\\w+)/0\\?wx_fmt=(\\w+)").replace(vm.url.toString(), "$1.$2")
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
                @Suppress("DEPRECATION")
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
        
        val resolve = folder.findFile("$prefix$suffix")?.takeIf { !it.isDirectory }
            ?: folder.createFile(mimeType, fileName)
            ?: throw Exception("........")
        
        vm.viewModelScope.launch(Dispatchers.IO) {
            val outputStream = contentResolver.openOutputStream(resolve.uri)
            if (vm.bitmap?.compress(format, 100, outputStream) == true) toast("保存成功")
            outputStream?.close()
            //TODO: 修改目录时无法发现已删除目录
        }
        
        return resolve.uri
    }
    
    /**
     * 复制到剪贴板
     */
    fun copy() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // 创建普通字符型ClipData
        val clipData = ClipData.newPlainText("copy image link", vm.url.toString())
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(clipData)
        toast("复制成功")
    }
    
    /**
     * 分享链接
     */
    fun shareURL(url: URL) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/*"
        intent.putExtra(EXTRA_TEXT, url.toString())
        startActivity(Intent.createChooser(intent, "分享到："))
    }
    
    fun openURL(url: URL) {
        val uri = Uri.parse(url.toString())
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = uri
        startActivity(intent)
    }
    
    fun toast(text: String) = Toast.makeText(this, text, LENGTH_SHORT).show()
    
    fun CoroutineScope.toast(text: String) {
        Looper.prepare()
        Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show()
        Looper.loop()
    }
    
    fun setRoot() {
        contentResolver.persistedUriPermissions.firstOrNull()?.let {
            vm.rootDir = DocumentFile.fromTreeUri(this, it.uri)
        }
    }
    
    fun openImage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val uri = save(null)
        //未保存时标记uri, 并在退出时删除
        if (vm.destroy[uri] != false) vm.destroy[uri] = true
        intent.setDataAndType(uri, "image/*")
        startActivity(intent)
    }
    
}