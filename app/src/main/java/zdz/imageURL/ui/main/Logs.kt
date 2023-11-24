package zdz.imageURL.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainViewModel
import zdz.libs.compose.ex.Heading
import zdz.libs.compose.ex.Title
import zdz.libs.compose.ex.str

@Destination
@Composable
fun Logs(vm: MainViewModel) {
    Title(
        modifier = Modifier.padding(12.dp),
        heading = { Heading(title = R.string.log.str) }
    ) {
        Text(
            text = vm.logger.getLog(),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
    }
}