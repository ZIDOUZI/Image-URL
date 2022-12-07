package zdz.imageURL.activity.main

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_PROCESS_TEXT
import android.content.Intent.EXTRA_TEXT
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.internal.format
import zdz.imageURL.*
import zdz.imageURL.R
import zdz.imageURL.ui.NavItem
import zdz.imageURL.ui.main.GuideScreen
import zdz.imageURL.ui.main.LogScreen
import zdz.imageURL.ui.main.MainScreen
import zdz.imageURL.ui.main.SettingsScreen
import zdz.imageURL.ui.theme.ImageURLTheme
import zdz.libs.url.getSourceCode
import zdz.libs.url.getUrl
import zdz.libs.url.urlReg
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val vm: MainViewModel by viewModels()
    
    lateinit var data: Data
    
    private val scope by lazy { vm.viewModelScope }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ImageURLTheme(
                darkTheme = vm.darkTheme.state,
                transparent = vm.transparent.state,
                alpha = vm.alpha.state,
            ) {
                if (vm.show) {
                    AlertDialog(
                        onDismissRequest = { vm.show = false },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_warning_24),
                                contentDescription = null,
                            )
                        },
                        title = {
                            Text(
                                text = stringResource(id = R.string.find_update),
                                style = typography.bodySmall
                            )
                        },
                        text = {
                            Text(
                                text = format(
                                    stringResource(id = R.string.dialog_text),
                                    MainViewModel.version,
                                    data.tagName,
                                    data.name
                                )
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    download(data.assets[0].browser_download_url)
                                    vm.show = false
                                },
                                content = { Text(text = stringResource(id = R.string.confirm)) }
                            )
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { vm.show = false },
                                content = { Text(text = stringResource(id = R.string.cancel)) }
                            )
                        }
                    )
                }
                
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
                        composable(NavItem.MainScr.route) { MainScreen(vm, this@MainActivity) }
                        composable(NavItem.HelpScr.route) { GuideScreen(BuildConfig.DEBUG) }
                        composable(NavItem.SettingsScr.route) {
                            SettingsScreen(vm, this@MainActivity)
                        }
                        composable(NavItem.LogScr.route) { LogScreen(vm) }
                    }
                }
            }
        }
        
        //启动时检测意图
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                when (intent.type) {
                    "text/plain" ->
                        intent.getStringExtra(EXTRA_TEXT)?.let { s ->
                            vm.text = s
                            recognize(true)
                        } ?: toast("分享错误.分享内容为空")
                }
            }
            
            Intent.ACTION_PROCESS_TEXT -> {
                intent.getStringExtra(EXTRA_PROCESS_TEXT)?.let { s ->
                    vm.text = s
                    recognize(true)
                } ?: toast("分享错误.分享内容为空")
            }
        }
        
        //初始化时将保有的永久uri访问地址读取到viewModel
        setRoot()
        
        scope.launch(Dispatchers.IO) {
            try {
                measureTimeMillis {
                    data = Json.decodeFromString(
                        Url(getString(R.string.update_url)).getSourceCode(skipSSL = true)
                    )
                    vm.show = data.isOutOfData(MainViewModel.version) && vm.autoCheck.state
                }.let { log("get remove info in $it millis time", c = Green) }
            } catch (e: SocketTimeoutException) {
                error(e, this)
            } catch (e: NullPointerException) {
                error(e, this)
            }
        }
    }
    
    fun chooseDir(uri: Uri) {
        registerForActivityResult(vm.dirContracts) {
            if (it != null) {
                if (vm.rootDir?.uri != it && vm.rootDir != null) {
                    try {
                        contentResolver.releasePersistableUriPermission(
                            vm.rootDir!!.uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        error(e)
                        toast("释放可持久 URI 时出错")
                    }
                }
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                vm.rootDir = DocumentFile.fromTreeUri(this, it)
            }
        }.launch(uri)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        //清除缓存
        vm.rootDir?.findFile("cacheFile")?.delete()
        externalCacheDir?.let { DocumentFile.fromFile(it) }?.findFile("cacheFile")?.delete()
    }
    
    private fun error(e: Throwable, scope: CoroutineScope? = null) {
        e.printStackTrace()
        log(e.message ?: "", c = Red)
        toast(e.message ?: "未知错误", scope)
    }
    
    private fun log(s: String, c: Color = Black) {
        vm.logs = AnnotatedString(
            "[${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}]",
            SpanStyle(fontWeight = W600)
        ) + AnnotatedString("${s.substringBefore("\n")}\n", SpanStyle(color = c)) +
                AnnotatedString("${s.substringAfter("\n", "")}\n\n") +
                vm.logs
    }
    
    /**
     * 识别输入框文本,改变[sourceURL][MainViewModel.sourceURL]
     */
    fun recognize(kill: Boolean = false): Job = scope.launch {
        if (vm.text.trim().isBlank()) return@launch
        if (!vm.text.contains(urlReg)) delay(1000)// TODO: 选择修改延时时间
        vm.sourceURL = try {
            (if (vm.text.trim().isDigitsOnly() && vm.preferredID.state != null)
                "${vm.preferredID.state}${vm.text}" else vm.text).run {
                getUrl() ?: idToUrl()
            }.apply {
                if (vm.autoJump.value && this.host == "www.pixiv.net") {
                    openURL(this)
                    if (vm.closeAfterProcess.value && kill) finishAndRemoveTask()
                } else process()
            }
        } catch (e: Throwable) {
            error(e)
            null
        }
    }
    
    /**
     * 处理输入框/分享文本.缓存图片链接到[bitmap][MainViewModel.bitmap]中.由于MainScreen中的图片使用
     * [bitmap][MainViewModel.bitmap]加载,因此缓存完成后会立即显示图像
     */
    fun process(res: String? = null) {
        val s = res ?: vm.text
        scope.launch(Dispatchers.IO) {
            try {
                measureTimeMillis {
                    vm.imgUrl = s.getUrl()?.let { imgUrlFromUrl(it) } ?: imgUrlFromID(s)
                    HttpClient(CIO) {
                        engine {
                            requestTimeout = 0
                        }
                    }.prepareGet(vm.imgUrl!!).execute {
                        vm.bitmap = BitmapFactory.decodeStream(it.body())
                    }
                }.let { log("process image in $it millis time", c = Green) }
            } catch (e: Throwable) {
                error(e, this)
            }
        }
    }
    
    /**
     *  调用系统下载器下载文件.默认下载完成后打开文件
     */
    private fun download(url: Url, open: Boolean = true) {
        val fileName = "${getString(R.string.app_name)}_release_${data.tagName}.apk"
        val request = DownloadManager.Request(Uri.parse(url.toString())).apply {
            setTitle(getString(R.string.app_name))
            setDescription(format(getString(R.string.download_tip), fileName))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = manager.enqueue(request)
        
        val query = DownloadManager.Query().setFilterById(id)
        var cursor: Cursor = manager.query(query)
        
        if (cursor.moveToFirst() && open) {
            scope.launch(Dispatchers.IO) {
                
                var status = DownloadManager.STATUS_RUNNING
                
                while (status == DownloadManager.STATUS_RUNNING) {
                    delay(1000)
                    cursor = manager.query(query)
                    require(cursor.moveToFirst())
                    status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                }
                
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uri = manager.getUriForDownloadedFile(id)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndTypeAndNormalize(uri, "application/vnd.android.package-archive")
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    startActivity(intent)
                }// TODO: 取消下载导致闪退
            }
        } else cursor.close()
    }
    
    /**
     * 分享图片
     */
    fun shareImage() =
        scope.launch {
            // 防止uri泄露.save()方法的uri以file://开头,应转化为content://开头
            val uri = FileProvider.getUriForFile(
                this@MainActivity,
                "${BuildConfig.DEBUG}.fileProvider",
                saveAsync().await().toFile()
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "分享图片"))
        }
    
    
    /**
     * 打开图片
     */
    fun openURL(url: Url) {
        val uri = Uri.parse(url.toString())
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = uri
        startActivity(intent)
    }
    
    /**
     * @param[name]文件名.默认为cacheFile.使用cacheFile作为文件名将保存在缓存目录,如果要使用下载时的原名请设为null
     */
    fun saveAsync(name: String? = "cacheFile"): Deferred<Uri> {
        
        //从网络获取的原图片名
        val sourceName = Regex("\\w+\\.(png|jpe?g)").find(vm.imgUrl.toString())?.value
            ?: Regex(".+biz_jpg/(\\w+)/0\\?wx_fmt=(\\w+)").replace(vm.imgUrl.toString(), "$1.$2")
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
        
        val folder = if (prefix == "cacheFile") externalCacheDir?.let { DocumentFile.fromFile(it) }
        else vm.rootDir
        
        return scope.async(Dispatchers.IO) {
            folder ?: return@async Uri.EMPTY.also {
                toast("MainActivity: save 无法获取文件夹", this)
            }
            val resolve = folder.findFile("$prefix$suffix")?.takeIf { it.isFile }
                ?: folder.createFile(mimeType, fileName)
                ?: throw Exception("........")
            measureTimeMillis {
                val outputStream = contentResolver.openOutputStream(resolve.uri)
                vm.bitmap?.compress(format, 100, outputStream)
                outputStream?.close()
                //TODO: 修改目录时无法发现已删除目录
            }.let { log("save image in $it millis time", c = Green) }
            return@async resolve.uri
        }
        
    }
    
    /**
     * 复制到剪贴板
     */
    fun copy(res: Any?) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // 创建普通字符型ClipData
        val clipData = ClipData.newPlainText("copy image link", res.toString())
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(clipData)
        toast("复制成功")
    }
    
    /**
     * 分享链接
     */
    fun shareUrl(url: Url) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/*"
        intent.putExtra(EXTRA_TEXT, url.toString())
        startActivity(Intent.createChooser(intent, "分享到："))
    }
    
    fun toast(text: String, scope: CoroutineScope? = null): Job? =
        scope?.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show()
        } ?: Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show().let { null }
    
    /**
     * 初始化[rootDir][MainViewModel.rootDir]
     */
    fun setRoot() {
        contentResolver.persistedUriPermissions.firstOrNull()?.let {
            vm.rootDir = DocumentFile.fromTreeUri(this, it.uri)
        }
    }
    
    fun openImage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //使用null,避免保存在缓存目录引发错误
        runBlocking(scope.coroutineContext) {
            saveAsync(null).await().takeIf { it != Uri.EMPTY }?.let {
                intent.setDataAndType(it, "image/*")
                startActivity(intent)
            }
        }
    }
    
}