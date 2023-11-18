package zdz.imageURL.activity.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zdz.imageURL.ui.main.Main
import zdz.imageURL.ui.theme.ImageURLTheme
import zdz.libs.preferences.compose.state

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val vm: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ImageURLTheme(
                darkTheme = vm.pf.darkTheme.state,
                alpha = vm.pf.alpha.state,
            ) {
                Main()
            }
        }
        
        vm.getRootDir(this)
        lifecycleScope.launch {
            vm.handleLaunchIntent(this@MainActivity)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        //清除缓存
        externalCacheDir?.let { DocumentFile.fromFile(it) }?.findFile("cacheFile")?.delete()
    }
}