package zdz.imageURL.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.adaptive.SplitResult
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.calculateDisplayFeatures
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zdz.imageURL.BuildConfig
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainViewModel
import zdz.imageURL.model.data.Type
import zdz.imageURL.utils.DataUnit
import zdz.imageURL.utils.copy
import zdz.imageURL.utils.findActivity
import zdz.imageURL.utils.getSourceCode
import zdz.imageURL.utils.sendText
import zdz.imageURL.utils.toast
import zdz.imageURL.utils.viewUri
import zdz.libs.compose.ex.Title
import zdz.libs.compose.ex.icon
import zdz.libs.compose.ex.ptr
import zdz.libs.compose.ex.repeatable
import zdz.libs.compose.ex.str
import zdz.libs.preferences.compose.delegator
import zdz.libs.preferences.compose.state
import zdz.libs.preferences.contracts.Pref
import kotlin.random.Random

@Composable
fun Home(
    vm: MainViewModel = hiltViewModel(),
    ctx: Context = LocalContext.current,
    queryRoot: () -> Unit = {},
) {
    var text by rememberSaveable { mutableStateOf("") }
    var count by remember { mutableIntStateOf(0) }
    Title(
        modifier = Modifier.padding(12.dp),
        heading = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = R.string.main_title.str,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.repeatable { count++ })
                Bonus(BuildConfig.DEBUG || count >= 114514, vm.pf.jm) { text = random() }
            }
        },
    ) { padding ->
        var sourceUrl by rememberSaveable { mutableStateOf<String?>(null) }
        var imgUrl by rememberSaveable { mutableStateOf<String?>(null) }
        var bitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
        // TODO: Use remember savable will cause `java.lang.RuntimeException: Could not copy bitmap to parcel blob.`
        // could not confirm that this bug is fixed or not
        var format = Bitmap.CompressFormat.PNG
        var filename = "image.png"
        
        var error by rememberSaveable { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        
        @Suppress("UnusedReceiverParameter")
        suspend fun CoroutineScope.process() {
            error = true
            if (text.isBlank()) return
            val (type, url) = vm.parseTextInput(text) ?: return
            
            if (type is Type.Unknown) return
            
            error = false
            delay(1000) // TODO: 选择修改延时时间
            
            if (text == "7399608") {
                count = 114514
                return
            }
            
            sourceUrl = url
            if (type !is Type.Extractable) {
                imgUrl = null
                if (vm.pf.autoJump.current()) ctx.viewUri(Uri.parse(url))
                return
            }
            
            try {
                Url(url).getSourceCode()
            } catch (e: Throwable) {
                if (e is SocketTimeoutException) ctx.toast("timeout")
                vm.logger.e("get source code error.", e)
                error = true
                return
            }.let(type::extractImageUrl).let { imageUrl ->
                imgUrl = imageUrl
                try {
                    vm.downloadImage(Url(imageUrl))
                } catch (e: Throwable) {
                    vm.logger.e("download image error.", e)
                    error = true
                    return
                }
            }?.let { (b, name, compressFormat) ->
                bitmap = b
                format = compressFormat ?: Bitmap.CompressFormat.PNG
                filename = name
            } ?: Unit.run {
                vm.logger.e("Can't decode bitmap")
                error = true
            }
        }
        
        LaunchedEffect(key1 = text, CoroutineScope::process)
        val shareError = R.string.share_error.str
        
        LaunchedEffect(key1 = Unit) {
            ctx.findActivity()?.run { // TODO: replace this with a better way
                intent?.run l@{
                    when (action) {
                        Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT)?.takeIf { type == "text/plain" }
                            ?.let { text = it } ?: toast(shareError)
                        
                        Intent.ACTION_PROCESS_TEXT -> getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.let {
                            text = it
                        } ?: toast(shareError)
                        
                        Intent.ACTION_VIEW -> data?.toString()?.takeUnless { it.isBlank() }
                            ?.let { text = it }
                        
                        else -> return@run
                    }
                    process()
                    if (vm.pf.closeAfterProcess.current() && sourceUrl?.substring(8)
                            ?.startsWith("www.pixiv.net") == true
                    // TODO: 考虑将逻辑放入Type中
                    ) finishAndRemoveTask()
                }
            }
        }
        
        TwoPane(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 12.dp),
            first = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    InputField(text = text, error = error, onTextChanged = { text = it }) {
                        if (error) return@InputField
                        scope.launch(block = CoroutineScope::process)
                    } // 56 + 9 * 2 dp
                    
                    sourceUrl?.let {
                        DisplayBox(text = it, ctx = ctx, view = !vm.pf.firstLink.state)
                    } // 48 dp
                    
                    imgUrl?.let {
                        DisplayBox(text = it, ctx = ctx, view = !vm.pf.secondLink.state)
                    } // 48 dp
                }
            },
            second = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    bitmap?.let {
                        AsyncImage(model = it,
                            contentDescription = "Image",
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .clickable { vm.viewImage(scope, ctx, it) }
                                .heightIn(100.dp, 300.dp),
                            placeholder = R.drawable.ic_loading.ptr,
                            error = R.drawable.ic_error.ptr) // max 300 dp
                        
                        vm.pf.dataUnit.state.let { scale ->
                            R.string.image_size.str.format(it.byteCount / scale, DataUnit[scale])
                        }.let { size ->
                            Text(text = size, modifier = Modifier.padding(top = 12.dp))
                        } // 16 + 12 dp
                        
                        Row {
                            IconButton(
                                onClick = { vm.shareImage(scope, ctx, it) },
                                modifier = Modifier.padding(6.dp),
                                content = R.drawable.ic_baseline_share_24.icon
                            ) // 40 + 6 * 2 dp
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (vm.getRootDir(ctx) == null) {
                                            ctx.toast(R.string.denied)
                                            queryRoot()
                                        } else {
                                            ctx.toast(R.string.save_succeed)
                                            val type = vm.pf.preferredMimeType.current() ?: format
                                            vm.saveImage(ctx, it, filename, type)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(6.dp),
                                content = R.drawable.ic_baseline_save_24.icon
                            ) // 40 + 6 * 2 dp
                            // TODO: 2022/6/4 适配未来的搜图项目
                        }
                    }
                }
            },
            strategy = { density, _, layoutCoordinates ->
                val (width, height) = layoutCoordinates.size
                if (width > height && height < with(density) { 550.dp.roundToPx() }) {
                    val splitX = width * 0.5f
                    val halfSplitWidthPixel = with(density) { 18.dp.toPx() } / 2f
                    SplitResult(
                        gapOrientation = Orientation.Vertical, gapBounds = Rect(
                            left = splitX - halfSplitWidthPixel,
                            top = 0f,
                            right = splitX + halfSplitWidthPixel,
                            bottom = height.toFloat(),
                        )
                    )
                } else {
                    val h = with(density) { 170.dp.toPx() }
                    SplitResult(
                        gapOrientation = Orientation.Horizontal,
                        gapBounds = Rect(0f, h, width.toFloat(), h)
                    )
                }
            },
            displayFeatures = ctx.findActivity()?.let { calculateDisplayFeatures(activity = it) }
                ?: emptyList())
    }
}

@Composable
fun InputField(text: String, error: Boolean, onTextChanged: (String) -> Unit, onDone: () -> Unit) =
    TextField(value = text,
        onValueChange = onTextChanged,
        label = { Text(text = R.string.input.str, fontSize = 13.sp) },
        placeholder = {
            Text(
                text = R.string.placeholder.str,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = TextFieldDefaults.colors(),
        isError = error,
        singleLine = true,
        maxLines = 1,
        modifier = Modifier
            .padding(vertical = 9.dp)
            .fillMaxWidth(),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            IconButton(
                onClick = onDone,
                enabled = !error,
                content = R.drawable.ic_baseline_find_replace_24.icon
            )
        })

@Composable
fun DisplayBox(text: String, ctx: Context, view: Boolean) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(1f)
            .clickable { ctx.copy(text) },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Icon(
        painter = R.drawable.ic_baseline_open_in_new_24.ptr,
        modifier = Modifier
            .padding(start = 12.dp)
            .heightIn(min = 48.dp)
            .clickable {
                if (view) ctx.viewUri(Uri.parse(text)) else ctx.sendText(text)
            },
        contentDescription = null,
    )
}

@Composable
fun Bonus(enabled: Boolean = true, pf: Pref<String?>, onClick: () -> Unit) =
    AnimatedVisibility(visible = enabled) {
        var string by pf.delegator
        Row {
            IconButton(onClick = onClick, content = R.drawable.ic_link.icon)
            IconButton(
                onClick = { string = "18comic.org" },
                content = R.drawable.baseline_auto_awesome_24.icon
            )
        }
    }

val list = listOf(
    "https://www.bilibili.com/video/BV1Ub41127ch",
    "https://www.bilibili.com/video/BV1w4411b7c3",
    "https://www.bilibili.com/video/BV1Ap4y1b7UC",
    
    "https://www.bilibili.com/video/BV1W54y1C7jy",
    "https://www.bilibili.com/video/BV1kL4y1V7H1",
    "https://www.bilibili.com/video/BV1DM411V72x",
    
    "https://www.bilibili.com/video/BV1U84y1d7Yf",
)

fun random() = list.random(Random(System.currentTimeMillis()))


@Composable
fun Scrollable() {
    Text(
        text = "1234567890".repeat(100),
        modifier = Modifier.padding(12.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}