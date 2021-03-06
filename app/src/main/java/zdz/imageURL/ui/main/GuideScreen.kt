package zdz.imageURL.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import zdz.imageURL.R
import zdz.libs.compose.Title

class DebugProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = listOf(true, false).asSequence()
}

@Preview(
    name = "GuideScreen", locale = "zh-rCN", showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun GuideScreen(@PreviewParameter(DebugProvider::class) debug: Boolean) {
    var show by remember { mutableStateOf(false) }

    Title(
        modifier = Modifier.padding(18.dp),
        scrollable = true,
        title = {
            Text(
                text = stringResource(id = R.string.guide),
                fontSize = 36.sp,
                modifier = Modifier.padding(8.dp).clickable { show = !show }
            )
        },
        subtitle = {
            if (show) {
                Row {
                    AsyncImage(
                        model = "https://pic.imoe.pw/2022/03/01/2f31bc49d9416.png",
                        contentDescription = "reward",
                        modifier = Modifier.sizeIn(
                            maxHeight = 200.dp,
                            maxWidth = 200.dp,
                            minHeight = 50.dp,
                            minWidth = 50.dp
                        )
                    )
                    Text(text = stringResource(id = R.string.r))
                }
            }
        }
    ) {
        Text(text = stringResource(id = R.string.guide_body1))
        Image(
            painter = painterResource(id = R.drawable.image_guide_1),
            contentDescription = "????????????1",
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Divider(color = MaterialTheme.colorScheme.onPrimary)
        Text(text = stringResource(id = R.string.guide_body2))
        Image(
            painter = painterResource(id = R.drawable.image_guide_2),
            contentDescription = "????????????2",
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Divider(color = MaterialTheme.colorScheme.onPrimary)
        Text(text = stringResource(id = R.string.guide_body3))
        Image(
            painter = painterResource(id = R.drawable.image_guide_3),
            contentDescription = "????????????3",
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Text(text = stringResource(id = R.string.guide_body4))
    }
}