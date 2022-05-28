package zdz.imageURL.ui.main.sub

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import zdz.imageURL.R
import zdz.imageURL.idToURL
import zdz.imageURL.process.getURL
import zdz.imageURL.ui.main.MainActivity
import zdz.imageURL.ui.main.MainViewModel
import zdz.imageURL.ui.theme.*
import zdz.libs.compose.Title

@Composable
fun MainScreen(navController: NavController, vm: MainViewModel, activity: MainActivity) {
    var count by remember { mutableStateOf(0) }
    
    Title(
        modifier = Modifier.padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClickableText(
                    text = AnnotatedString(stringResource(id = R.string.main_title)),
                    style = TextStyle(
                        fontSize = 36.sp,
                    ),
                    modifier = Modifier.padding(8.dp),
                ) { count++ }
                Bonus(activity, count >= 7 || vm.debug)
            }
        },
    ) {
        Row(
            modifier = Modifier.padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            var error: Boolean by remember { mutableStateOf(vm.error) }
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
                    error = vm.error
                },
                isError = vm.error,
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1f),
                keyboardActions = KeyboardActions(onDone = { activity.process() }),
                colors = textFieldColors(
                    textColor = if (isSystemInDarkTheme()) Gray400 else Gray500,
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
            Button(
                onClick = { activity.process() },
                enabled = !error,
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier.clickable { }
            ) {
                Text(text = stringResource(id = R.string.analysis))
            }
        }
        try {
            vm.text.getURL() ?: idToURL(vm.text)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }?.let {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = it.toString(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Button(
                    onClick = { activity.openURL(it) },
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(text = stringResource(id = R.string.open))
                }
            }
        }
        if (vm.url != null) {
            Row(
                modifier = Modifier.padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = vm.url.toString(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Button(
                    onClick = { activity.copy() },
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(text = stringResource(id = R.string.copy))
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
                Button(
                    modifier = Modifier.padding(6.dp),
                    onClick = { activity.shareURL(vm.url!!) },
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(text = stringResource(id = R.string.share_url))
                }
                Button(
                    onClick = { activity.shareImage() },
                    modifier = Modifier.padding(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(text = stringResource(id = R.string.share_file))
                }
                Button(
                    onClick = {
                        if (activity.contentResolver.persistedUriPermissions.isEmpty()) {
                            activity.toast("暂未授予访问权限")
                            activity.pickDir.launch(Uri.EMPTY)
                        } else if (vm.rootDir == null) {
                            activity.setRoot()
                        } else {
                            vm.destroy[activity.save(null)] = false
                        }
                    },
                    modifier = Modifier.padding(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}

@Composable
fun Bonus(activity: MainActivity, enabled: Boolean = true) {
    if (enabled) {
        Row {
            Button(onClick = {
                activity.process("https://www.bilibili.com/video/BV1Ap4y1b7UC")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_link),
                    contentDescription = "link"
                )
            }
        }
    }
}