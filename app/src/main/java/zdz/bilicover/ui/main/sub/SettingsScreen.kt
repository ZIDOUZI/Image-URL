package zdz.bilicover.ui.main.sub

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zdz.bilicover.R
import zdz.bilicover.pref.*
import zdz.bilicover.ui.Title
import zdz.bilicover.ui.main.MainActivity
import zdz.bilicover.ui.main.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel, activity: MainActivity) {
    
    val def = stringResource(id = R.string.check)
    val latest = stringResource(id = R.string.latest)
    val outOfData = stringResource(id = R.string.out_of_data)
    
    var summary by remember { mutableStateOf(def) }
    
    Title(
        modifier = Modifier.padding(18.dp),
        title = {
            Text(
                text = stringResource(R.string.settings),
                fontSize = 36.sp,
                modifier = Modifier.padding(8.dp)
            )
        },
    ) {
        PreferenceArea {
            CardPreference(
                title = stringResource(id = R.string.pick_dir),
                summary = stringResource(R.string.pick_dir_summary),
                onClick = { activity.pickDir.launch(Uri.EMPTY) },
            )
        }
        PreferenceGroup(text = stringResource(id = R.string.theme)) {
            SwitchPref(
                title = stringResource(id = R.string.transparent),
                summary = stringResource(id = R.string.experimental),
                checked = vm.transparent,
            ) {
                vm.transparent = it
                vm.prefs.transparent = it
            }
            SingleSelectChip(
                title = stringResource(id = R.string.dark_theme),
                selected = vm.darkTheme,
                onSelectedChange = { vm.darkTheme = it; vm.prefs.theme = it },
                entries = mapOf(
                    Pair(true, "夜间"),
                    Pair(false, "日间"),
                    Pair(null, "跟随系统"),
                )
            )
        }
        PreferenceArea {
            CardPreference(
                title = stringResource(id = R.string.checkUpdata),
                summary = summary,
                onClick = {
                    summary = if (activity.data.isOutOfData()) {
                        if (summary == outOfData) {
                            activity.download(activity.data.assets[0].browser_download_url)
                            activity.toast("下载中")
                            def
                        } else {
                            outOfData
                        }
                    } else latest
                },
            )
        }
    }
}