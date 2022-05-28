package zdz.imageURL.pref

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import zdz.imageURL.PreferenceGroupScope

@Composable
fun PreferenceGroupScope.SwitchPref(
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    summary: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(
            enabled = enabled,
            onClick = {
                onCheckedChange(!checked)
            },
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(),
        )
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = if (!this.isGroup) RoundedCornerShape(4.dp) else RectangleShape
        )
        .padding(start = 6.dp)
        .then(other = modifier),
    icon = icon,
    text = title,
    secondaryText = summary,
    trailing = {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    },
)


@Composable
fun PreferenceGroupScope.SwitchPref(
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    title: String,
    summary: String? = null,
    enabled: Boolean = true,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(
            enabled = enabled,
            onClick = { onCheckedChange(!checked) },
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(),
        )
        .padding(start = 6.dp)
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = if (!this.isGroup) RoundedCornerShape(4.dp) else RectangleShape
        )
        .zIndex(1f)
        .then(other = modifier),
    icon = icon?.let { { Icon(painter = it, contentDescription = "icon") } },
    text = { Text(text = title) },
    secondaryText = summary?.let { { Text(text = it, style = typography.bodyMedium) } },
    trailing = {
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    },
)