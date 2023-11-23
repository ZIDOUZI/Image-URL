package zdz.imageURL.ui.main

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
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
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zdz.imageURL.BuildConfig
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainNav
import zdz.imageURL.activity.main.MainViewModel
import zdz.imageURL.utils.OpenDocumentTree
import zdz.libs.compose.ex.AlertDialog
import zdz.libs.compose.ex.icon
import zdz.libs.compose.ex.str

@Composable
fun Main(vm: MainViewModel, startDestination: MainNav, ctx: Context = LocalContext.current) {
    
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
        val navBackStackEntry by navCtrl.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        NavigationBar {
            MainNav.entries.forEach { (name, label, icon) ->
                NavigationBarItem(selected = currentRoute == name,
                    onClick = { navCtrl.navigate(name) },
                    alwaysShowLabel = true,
                    icon = icon,
                    label = { Text(text = label, fontSize = 11.sp) })
            }
        }
    }) {
        NavHost(
            modifier = Modifier.padding(it),
            navController = navCtrl,
            startDestination = startDestination.name
        ) {
            composable(MainNav.MAIN.name) { Home(vm = vm, queryRoot = launcher::launch) }
            composable(MainNav.HELP.name) { Help() }
            composable(MainNav.SETTINGS.name) {
                Settings(queryRoot = launcher::launch, vm = vm, checkUpdate = vm::checkUpdate)
            }
            composable(MainNav.LOG.name) { Logs(vm = vm) }
        }
    }
}