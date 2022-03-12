package zdz.bilicover.pref

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import zdz.bilicover.PrefAreaInstance
import zdz.bilicover.PrefGroupInstance
import zdz.bilicover.PreferenceGroupScope

@Composable
fun PreferenceGroup(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable PreferenceGroupScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .then(modifier)
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Column {
            Divider()
            PrefGroupInstance.content()
        }
    }
}

@Composable
fun PreferenceArea(
    content: @Composable PreferenceGroupScope.() -> Unit
) = Column {
    PrefAreaInstance.content()
}