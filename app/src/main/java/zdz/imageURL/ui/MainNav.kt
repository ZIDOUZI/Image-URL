package zdz.imageURL.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import zdz.imageURL.R

enum class MainNav(
    @DrawableRes val icon: Int,
    @StringRes val string: Int,
) {
    MAIN(R.drawable.ic_baseline_home_24, R.string.home),
    HELP(R.drawable.ic_baseline_help_24, R.string.help),
    SETTINGS(R.drawable.ic_baseline_settings_24, R.string.settings),
    LOG(R.drawable.ic_baseline_history_24, R.string.log)
    ;
}