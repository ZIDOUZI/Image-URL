package zdz.bilicover.ui.main

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import zdz.bilicover.R
import zdz.bilicover.ui.Title
import zdz.bilicover.ui.theme.Black
import zdz.bilicover.ui.theme.White

@Preview(
    name = "GuideScreen", locale = "zh-rCN", showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun GuideScreen() {
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
            Row {
                Image(
                    painter = rememberImagePainter(data = ""),
                    contentDescription = "reward",
                    modifier = Modifier.sizeIn(maxHeight = 100.dp, maxWidth = 100.dp, minHeight = 50.dp, minWidth = 50.dp)
                )
            }
        }
    ) {
        Text(text = stringResource(id = R.string.guide_body1))
        Image(
            painter = painterResource(id = R.drawable.image_guide_1),
            contentDescription = "指导图片1",
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(text = stringResource(id = R.string.guide_body2))
        Image(
            painter = painterResource(id = R.drawable.image_guide_2),
            contentDescription = "指导图片2",
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(text = stringResource(id = R.string.guide_body3))
        Image(
            painter = painterResource(id = R.drawable.image_guide_3),
            contentDescription = "指导图片3",
            modifier = Modifier.padding(bottom = 12.dp),
        )
    }
}