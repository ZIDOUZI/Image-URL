package zdz.imageURL.activity.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import zdz.imageURL.R
import zdz.libs.compose.ex.icon
import zdz.libs.compose.ex.str

enum class MainNav(@DrawableRes private val icon: Int, @StringRes private val string: Int) {
    MAIN(R.drawable.ic_baseline_home_24, R.string.home),
    HELP(R.drawable.ic_baseline_help_24, R.string.help),
    SETTINGS(R.drawable.ic_baseline_settings_24, R.string.settings),
    LOG(R.drawable.ic_baseline_history_24, R.string.log)
    ;
    
    operator fun component1() = name
    @Composable
    operator fun component2() = string.str
    @Composable
    operator fun component3() = icon.icon
}