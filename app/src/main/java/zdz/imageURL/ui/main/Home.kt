package zdz.imageURL.ui.main

import android.content.Context
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
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.adaptive.SplitResult
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.TwoPaneStrategy
import com.google.accompanist.adaptive.calculateDisplayFeatures
import kotlinx.coroutines.launch
import zdz.imageURL.BuildConfig
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainViewModel
import zdz.imageURL.model.data.Type
import zdz.imageURL.utils.DataUnit
import zdz.imageURL.utils.copy
import zdz.imageURL.utils.findActivity
import zdz.imageURL.utils.sendText
import zdz.imageURL.utils.toast
import zdz.imageURL.utils.viewUri
import zdz.libs.compose.ex.Title
import zdz.libs.compose.ex.icon
import zdz.libs.compose.ex.ptr
import zdz.libs.compose.ex.repeatable
import zdz.libs.compose.ex.str
import zdz.libs.preferences.compose.state
import zdz.libs.preferences.model.set
import kotlin.random.Random

@Composable
fun Home(
    vm: MainViewModel,
    ctx: Context = LocalContext.current,
    queryRoot: () -> Unit,
) {
    Title(
        modifier = Modifier.padding(12.dp),
        heading = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = R.string.main_title.str,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.repeatable { vm.count++ })
                Bonus(
                    BuildConfig.DEBUG || vm.count >= 114514,
                    { vm.pf.jm.set(Type.JM.mirrorSites.first()) }) { vm.text = random() }
            }
        },
    ) { padding ->
        val scope = rememberCoroutineScope()
        
        vm.Refresh(context = ctx)
        
        TwoPane(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 12.dp),
            first = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    InputField(text = vm.text, error = vm.error, onTextChanged = { vm.text = it }) {
                        if (vm.error) return@InputField
                        scope.launch { vm.process(ctx) }
                    } // 56 + 9 * 2 dp
                    
                    val chooser = vm.pf.urlChooser.state
                    
                    vm.sourceUrl?.let {
                        DisplayBox(text = it, ctx = ctx, view = !vm.pf.firstLink.state, chooser)
                    } // 48 dp
                    
                    vm.imgUrl?.let {
                        DisplayBox(text = it, ctx = ctx, view = !vm.pf.secondLink.state, chooser)
                    } // 48 dp
                }
            },
            second = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    vm.bitmap?.let {
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
                            OutlinedIconButton(
                                onClick = { vm.shareImage(scope, ctx, it) },
                                modifier = Modifier.padding(6.dp),
                                content = R.drawable.ic_baseline_share_24.icon
                            ) // 40 + 6 * 2 dp
                            OutlinedIconButton(
                                onClick = {
                                    scope.launch {
                                        if (vm.getRootDir(ctx) == null) {
                                            ctx.toast(R.string.denied)
                                            queryRoot()
                                        } else {
                                            ctx.toast(R.string.save_succeed)
                                            vm.saveImage(ctx, it)
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
            strategy = strategy,
            displayFeatures = ctx.findActivity()?.let { calculateDisplayFeatures(activity = it) }
                ?: emptyList())
    }
}

val strategy: TwoPaneStrategy = TwoPaneStrategy { density, _, layoutCoordinates ->
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
fun DisplayBox(text: String, ctx: Context, view: Boolean, chooser: Boolean) = Row(
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
                if (view) ctx.viewUri(Uri.parse(text), chooser) else ctx.sendText(text)
            },
        contentDescription = null,
    )
}

@Composable
fun Bonus(enabled: Boolean = true, setText: () -> Unit, onClick: () -> Unit) =
    AnimatedVisibility(visible = enabled) {
        Row {
            IconButton(onClick = onClick, content = R.drawable.ic_link.icon)
            IconButton(
                onClick = setText,
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
    
    "https://www.bilibili.com/video/BV1Hy4y1r7k7",
    "https://www.bilibili.com/video/BV17W4y1E7hK",
    "https://www.bilibili.com/video/BV1wq4y1R7Kt",
    "https://www.bilibili.com/video/BV1rG411y7W3",
    
    "https://www.bilibili.com/video/BV1HC4y1W7ja",
    "https://www.bilibili.com/video/BV1QA4y1D7x8",
    "https://www.bilibili.com/video/BV1yT411H79u",
    
    "https://www.bilibili.com/video/BV1U84y1d7Yf",
    "https://www.bilibili.com/video/BV1ow41167CS",
)

private val seed = Random(System.currentTimeMillis())

fun random() = list.random(seed)