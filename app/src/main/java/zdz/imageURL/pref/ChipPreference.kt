package zdz.imageURL.pref

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import zdz.imageURL.PrefAreaInstance
import zdz.imageURL.PreferenceGroupScope
import zdz.imageURL.pref.core.core.PrefImpl

@Composable
fun ChipPreferenceItem(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean = true
) = Card(
    backgroundColor = if (isSelected) colorScheme.secondary else colorScheme.surface,
    contentColor = if (isSelected) colorScheme.onSecondary else colorScheme.onSurface,
    elevation = 2.dp,
    modifier = Modifier
        .padding(vertical = 5.dp)
        .padding(end = 2.dp),
    onClick = onSelect,
    enabled = enabled
) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(5.dp, 2.dp)
    )
}

@Composable
fun <T> PreferenceGroupScope.SingleSelectChip(
    title: String,
    selected: T,
    onSelectedChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true
) = ListItem(
    modifier = Modifier
        .padding(start = 6.dp)
        .zIndex(1f)
        .background(
            color = colorScheme.surfaceVariant,
            shape = if (!this.isGroup) RoundedCornerShape(4.dp) else RectangleShape
        )
        .then(other = modifier),
    icon = icon?.let { { Icon(painter = it, contentDescription = "icon") } },
    text = { Text(title, color = colorScheme.onSurfaceVariant) },
    secondaryText = {
        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(entries.toList()) { (key, value) ->
                ChipPreferenceItem(
                    text = value,
                    isSelected = key == selected,
                    onSelect = { onSelectedChange(key) },
                    enabled = enabled
                )
            }
        }
    },
)

@Composable
fun <T> PrefImpl<T>.SingleSelectChipPreference(
    title: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true
) {
    
    var selected by object : MutableState<T> {
        var state by remember { mutableStateOf(defaultValue) }
        override var value: T
            get() = state
            set(value) {
                state = value
                set(value)
            }
        
        override fun component1() = value
        override fun component2(): (T) -> Unit = { value = it }
    }
    
    PrefAreaInstance.SingleSelectChip(
        title = title,
        selected = selected,
        onSelectedChange = { selected = it },
        icon = icon,
        entries = entries,
        enabled = enabled,
        modifier = modifier
    )
}