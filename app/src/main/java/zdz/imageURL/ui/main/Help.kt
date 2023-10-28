package zdz.imageURL.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import zdz.imageURL.R
import zdz.libs.compose.ex.Heading
import zdz.libs.compose.ex.Title
import zdz.libs.compose.ex.ptr
import zdz.libs.compose.ex.str

@Composable
fun Help() {
    var show by remember { mutableStateOf(false) }
    
    Title(
        modifier = Modifier.padding(12.dp),
        heading = {
            Heading(title = R.string.guide.str, modifier = Modifier.clickable { show = !show }) {
                if (show) {
                    Row {
                        Image(
                            painter = R.drawable.photo_2022_09_18.ptr,
                            contentDescription = "reward",
                            modifier = Modifier.sizeIn(
                                maxHeight = 200.dp,
                                maxWidth = 200.dp,
                                minHeight = 50.dp,
                                minWidth = 50.dp
                            )
                        )
                        Text(text = R.string.r.str)
                    }
                }
            }
            
        },
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(text = stringResource(id = R.string.guide_body1))
            Image(
                painter = painterResource(id = R.drawable.image_guide_1),
                contentDescription = "guide iamge 1",
                modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary)
            Text(text = stringResource(id = R.string.guide_body2))
            Image(
                painter = painterResource(id = R.drawable.image_guide_2),
                contentDescription = "guide image 2",
                modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary)
            Text(text = stringResource(id = R.string.guide_body3))
            Image(
                painter = painterResource(id = R.drawable.image_guide_3),
                contentDescription = "guide image 3",
                modifier = Modifier.padding(vertical = 16.dp),
            )
            Text(text = stringResource(id = R.string.guide_body4))
        }
    }
}