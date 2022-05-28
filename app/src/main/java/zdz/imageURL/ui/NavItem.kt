package zdz.imageURL.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import zdz.imageURL.R

enum class NavItem(val route: String, @DrawableRes val icon: Int, @StringRes val string: Int) {
    MainScr("MAIN", R.drawable.ic_baseline_home_24, R.string.home),
    HelpScr("HELP", R.drawable.ic_baseline_help_24, R.string.help),
    SettingsScr("SETTINGS", R.drawable.ic_baseline_settings_24, R.string.settings),
    LogScr("INFO", R.drawable.ic_baseline_history_24, R.string.log)
}