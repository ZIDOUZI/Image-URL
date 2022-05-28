package zdz.imageURL.ui.main

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainActivity
import zdz.imageURL.activity.main.MainViewModel
import zdz.imageURL.supportIDS
import zdz.libs.compose.Title
import zdz.libs.compose.pref.*

@Composable
fun SettingsScreen(vm: MainViewModel, activity: MainActivity) {
    
    val default = R.string.check
    val latest = R.string.latest
    val outOfData = R.string.out_of_data
    
    var summary by remember { mutableStateOf(default) }
    
    Title(
        modifier = Modifier.padding(18.dp),
        scrollable = true,
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
                titleId = R.string.pick_dir,
                iconId = R.drawable.ic_baseline_folder_open_24,
                summaryId = R.string.pick_dir_summary,
            ) { activity.pickDir.launch(Uri.EMPTY) }
        }
        PreferenceGroup(textId = R.string.theme) {
            SwitchPref(
                pref = vm.transparent,
                iconId = R.drawable.ic_baseline_blur_on_24,
                summaryId = R.string.experimental,
            )
//            SliderPref(pref = vm.alpha, range = 0f..1f, enabled = vm.transparent.state)// TODO: ListItem 导致异常
            DropPref(
                pref = vm.darkTheme,
                entries = listOf(
                    true to R.string.dark,
                    false to R.string.light,
                    null to R.string.follow_system,
                ),
            )// TODO: LazyRow 导致异常
        }
        PreferenceGroup(textId = R.string.about) {
            CardPreference(
                titleId = R.string.check_update,
                iconId = R.drawable.ic_baseline_autorenew_24,
                summaryId = summary,
            ) {
                summary = if (activity.data.isOutOfData(MainViewModel.version)) {
                    vm.show = true
                    outOfData
                } else latest
            }
            SwitchPref(
                pref = vm.autoCheck,
                iconId = R.drawable.ic_baseline_check_circle_24,
            )
            CardPreference(
                titleId = R.string.share,
                iconId = R.drawable.ic_baseline_mobile_screen_share_24,
                summaryId = R.string.share_summary,
            ) {
                activity.shareURL(activity.data.htmlUrl)
            }
        }
        PreferenceGroup(textId = R.string.advanced) {
            SwitchPref(
                pref = vm.advanced,
                iconId = R.drawable.ic_baseline_app_settings_alt_24,
            )
            if (vm.advanced.state) {
                SwitchPref(
                    pref = vm.firstLink,
                    summaryId = R.string.first_link_summary,
                )
                SwitchPref(
                    pref = vm.secondLink,
                    summaryId = R.string.second_link_summary,
                )
                DropPref(
                    pref = vm.preferredID,
                    summaryId = R.string.preferred_id_summary,
                    entries = supportIDS,
                )
                SwitchPref(pref = vm.autoJump)
            }
        }
    }
}