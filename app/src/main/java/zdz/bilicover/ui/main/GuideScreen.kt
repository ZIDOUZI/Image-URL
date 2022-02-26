package zdz.bilicover.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zdz.bilicover.R
import zdz.bilicover.ui.Title

@Composable
fun GuideScreen() {
    Title(
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 18.dp)
            .verticalScroll(state = rememberScrollState()),
        title = {
            Text(text = stringResource(id = R.string.guide), fontSize = 36.sp)
        }) {
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