package zdz.imageURL.ui.main.sub

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zdz.imageURL.ui.main.MainViewModel

@Composable
fun LogScreen(vm: MainViewModel) {
    BasicText(
        text = vm.logs,
        modifier = Modifier.padding(16.dp).fillMaxSize()
    )
}