package zdz.imageURL.ui.main

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import zdz.imageURL.BuildConfig
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainActivity
import zdz.imageURL.activity.main.MainViewModel
import zdz.imageURL.ui.theme.Gray500
import zdz.imageURL.ui.theme.Red500
import zdz.imageURL.ui.theme.Teal300
import zdz.imageURL.ui.theme.Teal700
import zdz.libs.compose.StatusWrapper
import zdz.libs.compose.Title
import java.util.concurrent.CancellationException

@Composable
fun MainScreen(vm: MainViewModel, activity: MainActivity) {
    var count by remember { mutableStateOf(0) }
    
    Title(
        modifier = Modifier.padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.main_title),
                    fontSize = 36.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { count++ }
                )
                Bonus(vm, activity, count >= 7 || BuildConfig.DEBUG)
            }
        },
    ) {
        Row(
            modifier = Modifier.padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(
                value = vm.text,
                label = { Text(text = activity.getString(R.string.input), fontSize = 13.sp) },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.placeholder),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Gray500,
                    )
                },
                onValueChange = {
                    vm.text = it
                    vm.sourceURL = null
                    vm.job?.cancel(CancellationException("文本框文字改变,协程任务取消"))
                    vm.job = activity.recognize()
                },
                isError = vm.error,
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1f),
                keyboardActions = KeyboardActions(onDone = { vm.viewModelScope.launch { activity.process() } }),
                colors = textFieldColors(
                    textColor = colorScheme.onSurface,
                    focusedIndicatorColor = Teal300,
                    focusedLabelColor = Teal300,
                    cursorColor = Teal300,
                    unfocusedIndicatorColor = Teal700,
                    unfocusedLabelColor = Teal700,
                    errorIndicatorColor = Red500,
                    errorCursorColor = Red500,
                    errorLabelColor = Red500,
                )
            )
            IconButton(
                onClick = { vm.viewModelScope.launch { activity.process() } },
                enabled = !vm.error,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                StatusWrapper(enabled = !vm.error) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_find_replace_24),
                        contentDescription = "icon"
                    )
                }
            }
        }
        vm.sourceURL?.let {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = it.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activity.copy(it) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    onClick = {
                        if (vm.firstLink.state) activity.shareURL(it)
                        else activity.openURL(it)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_open_in_new_24),
                        contentDescription = stringResource(id = R.string.share_url)
                    )
                }
            }
        }
        vm.imgURL?.let {
            Row(
                modifier = Modifier.padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = it.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activity.copy(it) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    onClick = {
                        if (vm.secondLink.state) activity.shareURL(it)
                        else activity.openURL(it)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_open_in_new_24),
                        contentDescription = stringResource(id = R.string.share_url)
                    )
                }
            }
            AsyncImage(
                model = vm.bitmap,
                contentDescription = "Cover Image of Link",
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .heightIn(100.dp, 250.dp)
                    .clickable { activity.openImage() },
                placeholder = painterResource(id = R.drawable.ic_loading),
                error = painterResource(id = R.drawable.ic_error)
            )
            Row(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                IconButton(
                    modifier = Modifier.padding(6.dp),
                    onClick = { activity.shareImage() },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_share_24),
                        contentDescription = stringResource(id = R.string.share_file)
                    )
                }
                IconButton(
                    onClick = {
                        when {
                            activity.contentResolver.persistedUriPermissions.isEmpty() -> {
                                activity.toast(activity.getString(R.string.denied))
                                activity.pickDir.launch(Uri.EMPTY)
                            }
                            vm.rootDir == null -> activity.setRoot()
                            else -> {
                                activity.save(null)
                                activity.toast("保存成功")
                            }
                        }
                    },
                    modifier = Modifier.padding(6.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_save_24),
                        contentDescription = stringResource(id = R.string.save)
                    )
                }
                // TODO: 2022/6/4 适配未来的搜图项目
            }
        }
    }
}

@Composable
fun Bonus(vm: MainViewModel, activity: MainActivity, enabled: Boolean = true) {
    if (enabled) {
        Row {
            IconButton(onClick = {
                vm.viewModelScope.launch { activity.process("https://www.bilibili.com/video/BV1Ap4y1b7UC") }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_link),
                    contentDescription = "link"
                )
            }
        }
    }
}