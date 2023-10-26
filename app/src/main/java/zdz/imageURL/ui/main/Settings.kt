package zdz.imageURL.ui.main

import android.content.Context
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.Bitmap.CompressFormat.WEBP
import android.graphics.Bitmap.CompressFormat.WEBP_LOSSLESS
import android.graphics.Bitmap.CompressFormat.WEBP_LOSSY
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainViewModel
import zdz.imageURL.model.data.Type
import zdz.imageURL.utils.DataUnit
import zdz.libs.compose.ex.Direction
import zdz.libs.compose.ex.Heading
import zdz.libs.compose.ex.Title
import zdz.libs.compose.ex.getTransformBy
import zdz.libs.compose.ex.icon
import zdz.libs.compose.ex.str
import zdz.libs.preferences.compose.PreferenceArea
import zdz.libs.preferences.compose.component.DropDown
import zdz.libs.preferences.compose.component.LargeSlider
import zdz.libs.preferences.compose.component.SingleChip
import zdz.libs.preferences.compose.component.Switch
import zdz.libs.preferences.compose.component.TextField
import zdz.libs.preferences.compose.component.functional.Card
import zdz.libs.preferences.compose.delegator
import zdz.libs.preferences.compose.state

@Composable
fun Settings(
    queryRoot: () -> Unit,
    vm: MainViewModel = hiltViewModel(),
    ctx: Context = LocalContext.current,
    checkUpdate: suspend () -> Boolean?
) {
    
    val default = R.string.check
    val latest = R.string.latest
    val outOfData = R.string.out_of_data
    val failed = R.string.failed
    
    var summary by remember { mutableIntStateOf(default) }
    
    Title(
        modifier = Modifier.padding(12.dp),
        heading = {
            Heading(title = R.string.settings.str)
        },
    ) {
        PreferenceArea {
            Group(title = R.string.storage.str) {
                Card(
                    title = R.string.pick_dir.str,
                    icon = R.drawable.ic_baseline_folder_open_24.icon,
                    summary = R.string.pick_dir_summary.str,
                    onClick = { queryRoot() }
                )
            }
            Group(title = R.string.theme.str) {
                LargeSlider(
                    key = vm.pf.alpha,
                    title = R.string.alpha.str,
                    range = 0f..1f,
                    icon = R.drawable.ic_baseline_blur_on_24.icon
                )
                DropDown(
                    key = vm.pf.darkTheme, title = R.string.dark_theme.str, entries = mapOf(
                        true to R.string.dark.str,
                        false to R.string.light.str,
                        null to R.string.follow_system.str,
                    ),
                    icon = R.drawable.ic_baseline_brightness_4_24.icon
                )
            }
            Group(title = R.string.about.str) {
                val scope = rememberCoroutineScope()
                Card(
                    title = R.string.check_update.str, summary = summary.str, onClick = {
                        scope.launch {
                            summary = default
                            summary = when (checkUpdate()) {
                                true -> outOfData
                                false -> latest
                                null -> failed
                            }
                        }
                    },
                    icon = R.drawable.ic_baseline_autorenew_24.icon
                )
                Switch(
                    key = vm.pf.autoCheck,
                    title = R.string.auto_check.str,
                    icon = R.drawable.ic_baseline_check_circle_24.icon,
                )
                Card(
                    title = R.string.share.str,
                    icon = R.drawable.ic_baseline_mobile_screen_share_24.icon,
                    onClick = { vm.sendShareLink(ctx) },
                    summary = R.string.share_summary.str,
                )
                Card(
                    title = R.string.feedback.str,
                    onClick = { vm.openFeedback(ctx) },
                    icon = R.drawable.ic_baseline_feedback_24.icon,
                    summary = R.string.feedback_summary.str,
                )
            }
            Group(title = R.string.advanced.str) {
                var expanded by vm.pf.advanced.delegator
                AnimatedContent(
                    targetState = expanded,
                    label = "expand",
                    transitionSpec = getTransformBy(Direction.Vertical.Down)
                ) { visible ->
                    Column {
                        if (!visible) {
                            Card(
                                title = R.string.advanced.str, onClick = { expanded = true },
                                icon = R.drawable.ic_baseline_app_settings_alt_24.icon
                            )
                        } else {
                            Switch(
                                key = vm.pf.firstLink,
                                title = R.string.first_link.str,
                                summary = R.string.first_link_summary.str
                            )
                            Switch(
                                key = vm.pf.secondLink,
                                title = R.string.second_link.str,
                                summary = R.string.second_link_summary.str
                            )
                            val jm = vm.pf.jm.state
                            DropDown(
                                key = vm.pf.preferredID,
                                title = R.string.preferred_id.str,
                                summary = R.string.preferred_id_summary.str,
                                entries = buildMap {
                                    Type.identityEntries.forEach { put(it, it.name) }
                                    if (jm != null) put(Type.JM, Type.JM.name)
                                    put(null, R.string.disabled.str)
                                }
                            )
                            jm?.let {
                                TextField(
                                    key = vm.pf.jm,
                                    title = "Mirror Site",
                                    enabled = vm.pf.preferredID.state is Type.JM
                                )
                            }
                            Switch(
                                key = vm.pf.closeAfterProcess,
                                title = R.string.close_after_process.str,
                            )
                            Switch(key = vm.pf.autoJump, title = R.string.auto_jump.str)
                            Switch(
                                key = vm.pf.chooseOpener,
                                title = R.string.choose_opener.str,
                                summary = R.string.choose_opener_summary.str
                            ) // TODO: Change to multiple chips
                            SingleChip(
                                key = vm.pf.preferredMimeType,
                                title = R.string.image_format.str,
                                entries = buildMap {
                                    put(PNG, "PNG")
                                    put(JPEG, "JPEG")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        put(WEBP_LOSSLESS, R.string.webp_lossless.str)
                                        put(WEBP_LOSSY, R.string.webp_lossy.str)
                                    } else {
                                        put(WEBP, "WEBP")
                                    }
                                    put(null, R.string.follow_image.str)
                                },
                                flowRow = true
                            )
                            SingleChip(
                                key = vm.pf.dataUnit,
                                entries = DataUnit,
                                title = R.string.data_unit.str,
                                flowRow = true
                            )
                        }
                    }
                }
            }
        }
    }
}
