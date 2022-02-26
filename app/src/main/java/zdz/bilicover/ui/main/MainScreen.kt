package zdz.bilicover.ui.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.TextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import zdz.bilicover.R
import zdz.bilicover.abPath
import zdz.bilicover.ui.Title
import zdz.bilicover.ui.theme.*
import zdz.libs.url.urlReg
import java.net.URL

@Composable
fun MainScreen(navController: NavController, vm: MainViewModel, activity: MainActivity) {
    
    val pickDir = rememberLauncherForActivityResult(
        contract = vm.dirContracts
    ) {
        if (it != null) {
            if (vm.uri.toString().isNotBlank() && vm.uri.toString() != it.toString()) {
                activity.contentResolver.releasePersistableUriPermission(
                    vm.uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            activity.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            vm.uri = it
            vm.path = it.abPath
        }
    }
    
    val uriList = LocalContext.current.contentResolver.persistedUriPermissions
    
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Title(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.main_title),
                        fontSize = 36.sp,
                        modifier = Modifier.padding(8.dp),
                    )
                TestUnit(vm, activity)// TODO:delete this
                }
            },
            fab = {
                FloatingActionButton(onClick = { pickDir.launch(Uri.EMPTY) }, shape = CircleShape) {
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
                        Text(
                            text = activity.getString(R.string.input), fontSize = 13.sp
                        )
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
                    colors = textFieldColors(
                        textColor = LocalContentColor.current,//TODO: 修改颜色
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
            if (vm.url != null) {
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
                        onClick = { activity.shareURL() },
                        modifier = Modifier.padding(6.dp),
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
                                pickDir.launch(Uri.EMPTY)
                            } else {
                                activity.save(vm.path, null)
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
//            Text(text = if (vm.bitmap != null) "has" else "hasn't")// TODO: Delete this
        }
    }
    
}

@Composable
fun TestUnit(vm: MainViewModel, activity: MainActivity) {
    Row {
        Button(onClick = {
            activity.cache("https://www.bilibili.com/video/av339319925")
        }) {
            Icon(painter = painterResource(id = R.drawable.ic_link), contentDescription = "link")
        }
    }
}