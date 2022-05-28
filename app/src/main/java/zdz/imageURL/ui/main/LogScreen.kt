package zdz.imageURL.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zdz.imageURL.R
import zdz.imageURL.activity.main.MainViewModel
import zdz.libs.compose.Title

@Composable
fun LogScreen(vm: MainViewModel) {
    Title(
        modifier = Modifier.padding(18.dp),
        scrollable = true,
        title = {
            Text(
                text = stringResource(id = R.string.log),
                fontSize = 36.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    ) {
        Text(
            text = vm.logs,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        )
    }
}