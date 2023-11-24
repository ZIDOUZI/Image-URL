package zdz.imageURL.activity.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import zdz.imageURL.R
import zdz.imageURL.ui.main.destinations.HelpDestination
import zdz.imageURL.ui.main.destinations.HomeDestination
import zdz.imageURL.ui.main.destinations.LogsDestination
import zdz.imageURL.ui.main.destinations.SettingsDestination
import zdz.libs.compose.ex.icon
import zdz.libs.compose.ex.str

enum class MainNav(
    private val destination: DirectionDestinationSpec,
    @StringRes private val title: Int,
    @DrawableRes private val icon: Int
) {
    Home(HomeDestination, R.string.home, R.drawable.ic_baseline_home_24),
    Help(HelpDestination, R.string.help, R.drawable.ic_baseline_help_24),
    Settings(SettingsDestination, R.string.settings, R.drawable.ic_baseline_settings_24),
    Logs(LogsDestination, R.string.log, R.drawable.ic_baseline_history_24), ;
    
    operator fun component1() = destination
    @Composable
    operator fun component2() = title.str
    @Composable
    operator fun component3() = icon.icon
}