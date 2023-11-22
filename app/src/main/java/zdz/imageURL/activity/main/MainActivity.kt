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
import zdz.imageURL.ui.MainNav
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
        
        val startDestination = if (handleLaunchIntent() == null) MainNav.SETTINGS else MainNav.MAIN
        lifecycleScope.launch {
            vm.run {
                process(this@MainActivity)
                if (imgUrl == null && pf.autoJump.current() && pf.closeAfterProcess.current()) {
                    toast(getString(R.string.redirect_toast, sourceUrl))
                    finishAndRemoveTask()
                }
            }
        }
        
        setContent {
            ImageURLTheme(
                darkTheme = vm.pf.darkTheme.state,
                alpha = vm.pf.alpha.state,
            ) {
                Main(startDestination = startDestination)
            }
        }
        
        vm.getRootDir(this)
    }
    
    private fun handleLaunchIntent(): Unit? = intent.run l@{
        when (action) {
            Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT)?.takeIf { type == "text/plain" }
                ?.let { vm.text = it } ?: toast(shareError)
            
            Intent.ACTION_PROCESS_TEXT -> getStringExtra(Intent.EXTRA_PROCESS_TEXT)
                ?.let { vm.text = it } ?: toast(shareError)
            
            Intent.ACTION_VIEW -> data?.toString()?.takeUnless { it.isBlank() }
                ?.let { vm.text = it } ?: toast(shareError)
            
            Intent.ACTION_PASTE -> vm.processClipboard() ?: toast(shareError)
            
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