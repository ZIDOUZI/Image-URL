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
import androidx.activity.result.ActivityResultLauncher
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
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.internal.format
import zdz.imageURL.*
import zdz.imageURL.R
import zdz.imageURL.process.getSourceCode
import zdz.imageURL.process.getURL
import zdz.imageURL.process.skipSSL
import zdz.imageURL.process.urlReg
import zdz.imageURL.ui.NavItem
import zdz.imageURL.ui.main.GuideScreen
import zdz.imageURL.ui.main.LogScreen
import zdz.imageURL.ui.main.MainScreen
import zdz.imageURL.ui.main.SettingsScreen
import zdz.imageURL.ui.theme.ImageURLTheme
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val vm: MainViewModel by viewModels()
    
    lateinit var pickDir: ActivityResultLauncher<Uri?>
    
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
                        composable(NavItem.MainScr.route) {
                            MainScreen(vm, this@MainActivity)
                        }
                        composable(NavItem.HelpScr.route) { GuideScreen(BuildConfig.DEBUG) }
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
        
        //?????????????????????
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                when (intent.type) {
                    "text/plain" ->
                        intent.getStringExtra(EXTRA_TEXT)?.let { s ->
                            vm.text = s
                            recognize()
                        } ?: toast("????????????.??????????????????")
                }
            }
            Intent.ACTION_PROCESS_TEXT -> {
                intent.getStringExtra(EXTRA_PROCESS_TEXT)?.let { s ->
                    vm.text = s
                    recognize()
                } ?: toast("????????????.??????????????????")
            }
        }
        
        //??????????????????????????????uri?????????????????????viewModel
        setRoot()
        
        // TODO: ??????????????????.???????????????Activity??????
        pickDir = registerForActivityResult(vm.dirContracts) {
            if (it != null) {
                if (vm.rootDir?.uri != it && vm.rootDir != null) {
                    try {
                        contentResolver.releasePersistableUriPermission(
                            vm.rootDir!!.uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        error(e)
                        toast("??????????????? URI ?????????")
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
            measureTimeMillis {
                skipSSL()
                try {
                    data = Json.decodeFromString(
                        URL(getString(R.string.update_url)).getSourceCode()
                            ?: throw NullPointerException("????????????????????????,?????????????????????")
                    )
                    vm.show = data.isOutOfData(MainViewModel.version) && vm.autoCheck.state
                } catch (e: SocketTimeoutException) {
                    error(e)
                } catch (e: NullPointerException) {
                    error(e)
                }
            }.let { log("get remove info in $it millis time", c = Green) }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        //????????????
        vm.rootDir?.findFile("cacheFile")?.delete()
        externalCacheDir?.let { DocumentFile.fromFile(it) }?.findFile("cacheFile")?.delete()
    }
    
    private fun error(e: Throwable) {
        e.printStackTrace()
        log(e.message ?: "", c = Red)
        toast(e.message ?: "????????????")
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
     * ?????????????????????,??????[sourceURL][MainViewModel.sourceURL]
     */
    fun recognize(): Job = scope.launch {
        if (!vm.text.contains(urlReg)) delay(1000)// TODO: ????????????????????????
        vm.sourceURL = try {
            vm.text.takeIf(String::isNotBlank)?.run {
                getURL() ?: takeIf { it.matches(numReg) || vm.preferredID.value != null }
                    ?.let { vm.preferredID.state!!.name + it }?.idToURL()
                ?: idToURL()
            }?.apply {
                if (vm.autoJump.value && this.host == "www.pixiv.net") {
                    openURL(this)
                    if (vm.closeAfterProcess.value) finishAndRemoveTask()
                } else process()
            }
        } catch (e: Throwable) {
            error(e)
            null
        }
    }
    
    /**
     * ???????????????/????????????.?????????????????????[bitmap][MainViewModel.bitmap]???.??????MainScreen??????????????????
     * [bitmap][MainViewModel.bitmap]??????,??????????????????????????????????????????
     */
    fun process(res: String? = null) {
        val s = res ?: vm.text
        scope.launch(Dispatchers.IO) {
            measureTimeMillis {
                try {
                    vm.imgURL = s.getURL()?.let { imgURLFromURL(it) } ?: imgURLFromId(s)
                    val inputStream: InputStream = vm.imgURL!!.openStream()
                    vm.bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                } catch (e: Throwable) {
                    error(e)
                }
            }.let { log("process image in $it millis time", c = Green) }
        }
    }
    
    /**
     *  ?????????????????????????????????.?????????????????????????????????
     */
    private fun download(url: URL, open: Boolean = true) {
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
                }// TODO: ????????????????????????
            }
        } else cursor.close()
    }
    
    /**
     * ????????????
     */
    fun shareImage() {
        // ??????uri??????.save()?????????uri???file://??????,????????????content://??????
        val uri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileProvider",
            save().toFile()
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "????????????"))
    }
    
    /**
     * ????????????
     */
    fun openURL(url: URL) {
        val uri = Uri.parse(url.toString())
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = uri
        startActivity(intent)
    }
    
    /**
     * @param[name]?????????.?????????cacheFile.??????cacheFile???????????????????????????????????????,??????????????????????????????????????????null
     */
    fun save(name: String? = "cacheFile"): Uri {
        
        //??????????????????????????????
        val sourceName = Regex("\\w+\\.(png|jpe?g)").find(vm.imgURL.toString())?.value
            ?: Regex(".+biz_jpg/(\\w+)/0\\?wx_fmt=(\\w+)").replace(vm.imgURL.toString(), "$1.$2")
        val prefix = name ?: sourceName.substring(0 until sourceName.lastIndexOf('.'))
        val suffix = sourceName.substring(sourceName.lastIndexOf('.'))
        val fileName = prefix + suffix
        
        //??????????????????
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
            else -> throw IllegalStateException("MainActivity: save ?????????????????????")
        }
        
        val folder = if (prefix == "cacheFile") {
            externalCacheDir?.let { DocumentFile.fromFile(it) }
        } else {
            vm.rootDir
        } ?: return Uri.EMPTY.apply { toast("MainActivity: save ?????????????????????") }
        
        
        val resolve = folder.findFile("$prefix$suffix")?.takeIf { !it.isDirectory }
            ?: folder.createFile(mimeType, fileName)
            ?: throw Exception("........")
        
        scope.launch(Dispatchers.IO) {
            measureTimeMillis {
                val outputStream = contentResolver.openOutputStream(resolve.uri)
                vm.bitmap?.compress(format, 100, outputStream)
                outputStream?.close()
                //TODO: ??????????????????????????????????????????
            }.let { log("save image in $it millis time", c = Green) }
        }
        
        return resolve.uri
    }
    
    /**
     * ??????????????????
     */
    fun copy(res: Any?) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // ?????????????????????ClipData
        val clipData = ClipData.newPlainText("copy image link", res.toString())
        // ???ClipData?????????????????????????????????
        cm.setPrimaryClip(clipData)
        toast("????????????")
    }
    
    /**
     * ????????????
     */
    fun shareURL(url: URL) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/*"
        intent.putExtra(EXTRA_TEXT, url.toString())
        startActivity(Intent.createChooser(intent, "????????????"))
    }
    
    fun toast(text: String) = Toast.makeText(this, text, LENGTH_SHORT).show()
    
    private fun CoroutineScope.toast(text: String): Job =
        launch(Dispatchers.Main) { Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show() }
    
    /**
     * ?????????[rootDir][MainViewModel.rootDir]
     */
    fun setRoot() {
        contentResolver.persistedUriPermissions.firstOrNull()?.let {
            vm.rootDir = DocumentFile.fromTreeUri(this, it.uri)
        }
    }
    
    fun openImage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //??????null,???????????????????????????????????????
        save(null).takeIf { it != Uri.EMPTY }?.let {
            intent.setDataAndType(it, "image/*")
            startActivity(intent)
        }
    }
    
}