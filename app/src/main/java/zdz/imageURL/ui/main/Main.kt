package zdz.imageURL.ui.main

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.rememberNavHostEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zdz.imageURL.BuildConfig
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainNav
import zdz.imageURL.activity.main.MainViewModel
import zdz.imageURL.ui.main.destinations.Destination
import zdz.imageURL.utils.OpenDocumentTree
import zdz.libs.compose.ex.AlertDialog
import zdz.libs.compose.ex.icon
import zdz.libs.compose.ex.str

@Composable
fun Main(vm: MainViewModel, startDestination: Destination, ctx: Context = LocalContext.current) {
    
    val launcher = rememberLauncherForActivityResult(contract = OpenDocumentTree) { closure ->
        vm.getRootDir(ctx)?.uri.let { closure(ctx, it) }
    }
    
    val scope = rememberCoroutineScope()
    
    vm.data?.let {
        AlertDialog(
            confirmLabel = R.string.confirm.str,
            onConfirm = {
                scope.launch(Dispatchers.IO) { vm.downloadUpdate(it, ctx = ctx) }
                vm.data = null
            },
            dismissLabel = R.string.cancel.str,
            onDismiss = { vm.data = null },
            title = R.string.find_update.str,
            content = {
                Text(R.string.dialog_text.str.format(BuildConfig.VERSION, it.tagName, it.name))
            },
            icon = R.drawable.ic_baseline_warning_24.icon
        )
    }
    
    val navCtrl = rememberNavController()
    Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
        val currentRoute by navCtrl.appCurrentDestinationAsState()
        NavigationBar {
            MainNav.entries.forEach { (dest, label, icon) ->
                NavigationBarItem(
                    selected = currentRoute == dest,
                    onClick = { navCtrl.navigate(dest) },
                    alwaysShowLabel = true,
                    label = { Text(text = label) },
                    icon = icon,
                )
            }
        }
    }) {
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(it),
            engine = rememberNavHostEngine(
                rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING
            ),
            navController = navCtrl,
            startRoute = startDestination,
            dependenciesContainerBuilder = {
                dependency<MainViewModel, _>(NavGraphs.root) { vm }
                dependency<() -> Unit, _>(NavGraphs.root) { { launcher.launch(Unit) } }
                dependency<suspend () -> Boolean?, _>(NavGraphs.root) { vm::checkUpdate }
            }
        )
    }
}