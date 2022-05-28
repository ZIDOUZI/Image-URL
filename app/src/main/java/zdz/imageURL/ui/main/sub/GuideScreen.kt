package zdz.imageURL.ui.main.sub

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import zdz.imageURL.R
import zdz.imageURL.ui.Title
import zdz.imageURL.ui.theme.Black
import zdz.imageURL.ui.theme.White

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
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 18.dp)
            .verticalScroll(state = rememberScrollState()),
        title = {
            ClickableText(
                text = AnnotatedString(stringResource(id = R.string.guide)),
                style = TextStyle(
                    fontSize = 36.sp,
                    color = if (isSystemInDarkTheme()) White else Black
                ),
                modifier = Modifier.padding(8.dp),
            ) { show = true }
        },
        subtitle = {
            if (debug || show) {
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
            contentDescription = "指导图片1",
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Divider(color = MaterialTheme.colorScheme.onPrimary)
        Text(text = stringResource(id = R.string.guide_body2))
        Image(
            painter = painterResource(id = R.drawable.image_guide_2),
            contentDescription = "指导图片2",
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Divider(color = MaterialTheme.colorScheme.onPrimary)
        Text(text = stringResource(id = R.string.guide_body3))
        Image(
            painter = painterResource(id = R.drawable.image_guide_3),
            contentDescription = "指导图片3",
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Text(text = stringResource(id = R.string.guide_body4))
    }
}