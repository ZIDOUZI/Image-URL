package zdz.imageURL.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Test() {
    Row {
        Text(
            text = "http://patorjk.com/misc/scrollingtext/timewaster.php?text=i+love+you%EF%B8%8F&autoscroll=OFF&duration=20",
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { /*TODO*/ }, contentPadding = PaddingValues(horizontal = 10.dp)) {
            Text(text = "使用")
        }
    }
}

@Preview
@Composable
fun Test2() {
    Surface(color = Color.White) {
        Test()
    }
}