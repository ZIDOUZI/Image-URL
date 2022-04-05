package zdz.bilicover.ui.main.sub

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material.TextField
import androidx.compose.material3.*
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
import coil.compose.rememberImagePainter
import zdz.bilicover.R
import zdz.bilicover.ui.NavItem
import zdz.bilicover.ui.main.MainActivity
import zdz.bilicover.ui.main.MainViewModel
import zdz.bilicover.ui.theme.*
import zdz.bilicover.url.urlReg
import zdz.libs.compose.Title

@Composable
fun MainScreen(navController: NavController, vm: MainViewModel, activity: MainActivity) {
    
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var count by remember { mutableStateOf(0) }
        Title(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClickableText(
                        text = AnnotatedString(stringResource(id = R.string.main_title)),
                        style = TextStyle(
                            fontSize = 36.sp,
                            color = if (vm.darkTheme ?: isSystemInDarkTheme()) White else Black
                        ),
                        modifier = Modifier.padding(8.dp),
                    ) { count++ }
                    Bonus(activity, count >= 7 || vm.debug)
                }
            },
            fab = {
                FloatingActionButton(
                    onClick = { navController.navigate(NavItem.SettingsScr.route) },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Set save position"
                    )
                }
            }
        ) {
            Row(
                modifier = Modifier.padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                var value: String by remember { mutableStateOf("") }
                var error: Boolean by remember { mutableStateOf(true) }
                TextField(
                    value = value,
                    label = {
                        Text(text = activity.getString(R.string.input), fontSize = 13.sp)
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.placeholder),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Gray500,
                        )
                    },
                    onValueChange = {
                        value = it
                        error = !it.contains(urlReg)
                    },
                    isError = error,
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .weight(1f),
                    keyboardActions = KeyboardActions(onGo = { activity.cache(value) }),
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
                    onClick = { activity.cache(value) },
                    enabled = !error,
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(text = stringResource(id = R.string.analysis))
                }
            }
            if (vm.url != null) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = vm.url.toString(), modifier = Modifier.weight(1f))
                    Button(
                        onClick = { activity.copy() },
                        contentPadding = PaddingValues(horizontal = 10.dp),
                    ) {
                        Text(text = stringResource(id = R.string.copy))
                    }
                }
                Image(
                    contentDescription = "Cover Image of Link",
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    painter = rememberImagePainter(data = vm.bitmap) {
                        placeholder(R.drawable.ic_loading)
                        error(R.drawable.ic_error)
                    },
                    modifier = Modifier.heightIn(100.dp, 250.dp)
                )
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Button(
                        modifier = Modifier.padding(6.dp),
                        onClick = { vm.url?.let { activity.shareURL(it) } ?: activity.toast("尚未解析哦") },
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
                                activity.save(null)
                            }
                        },
                        modifier = Modifier.padding(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                    ) {
                        Text(text = stringResource(id = R.string.save))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { navController.navigate("guide") },
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(text = stringResource(R.string.help))
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
                activity.cache("https://www.bilibili.com/video/BV1Ap4y1b7UC")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_link),
                    contentDescription = "link"
                )
            }
        }
    }
}