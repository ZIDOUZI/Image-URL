package zdz.imageURL.pref

import androidx.compose.foundation.clickable
import androidx.compose.material.ListItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import zdz.imageURL.PreferenceGroupScope

@Composable
fun PreferenceGroupScope.CardPreference(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String,
    summary: String? = null,
    onClick: () -> Unit = {},
) = ListItem(
    modifier = modifier
        .zIndex(1f)
        .clickable(
            onClick = onClick
        ),
    icon = { icon?.let { Icon(painter = it, contentDescription = "icon") } },
    text = { Text(text = title, fontSize = 16.sp) },
    secondaryText = { summary?.let { Text(text = it, fontSize = 11.sp) } },
)