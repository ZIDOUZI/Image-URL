package zdz.imageURL.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zdz.imageURL.R
import zdz.imageURL.ui.main.Main
import zdz.imageURL.ui.theme.ImageURLTheme
import zdz.imageURL.utils.toast
import zdz.libs.preferences.compose.state

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val vm: MainViewModel by viewModels()
    
    private val shareError by lazy { getString(R.string.share_error) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val startDestination =
            if (vm.handleLaunchIntent() == null) MainNav.SETTINGS else MainNav.MAIN
        
        setContent {
            ImageURLTheme(
                darkTheme = vm.pf.darkTheme.state,
                alpha = vm.pf.alpha.state,
            ) {
                Main(vm = vm, startDestination = startDestination)
            }
        }
        
        lifecycleScope.launch {
            if (intent.action != Intent.ACTION_MAIN && intent.action != Intent.ACTION_APPLICATION_PREFERENCES)
                vm.onLaunch()
            if (vm.pf.autoCheck.current()) vm.checkUpdate()
        }
    }
    
    private suspend fun MainViewModel.onLaunch() {
        process(this@MainActivity)
        if (imgUrl == null && pf.autoJump.current() && pf.closeAfterProcess.current()) {
            toast(getString(R.string.redirect_toast, sourceUrl))
            finishAndRemoveTask()
        }
    }
    
    private fun MainViewModel.handleLaunchIntent(): Unit? = intent.run l@{
//        Debug.waitForDebugger()
        when (action) {
            Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT)?.takeIf { type == "text/plain" }
                ?.let { text = it } ?: toast(shareError)
            
            Intent.ACTION_PROCESS_TEXT -> getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.let {
                    text = it
                } ?: toast(shareError)
            
            Intent.ACTION_VIEW -> data?.toString()?.takeUnless { it.isBlank() }?.let { text = it }
                ?: toast(shareError)
            
            Intent.ACTION_PASTE -> processClipboard() ?: toast(shareError)
            
            Intent.ACTION_APPLICATION_PREFERENCES -> return null
            
            else -> return@l
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        //清除缓存
        externalCacheDir?.let { DocumentFile.fromFile(it) }?.findFile("cacheFile")?.delete()
    }
}