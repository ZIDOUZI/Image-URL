package zdz.imageURL

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Immutable

@LayoutScopeMarker
@Immutable
interface PreferenceGroupScope {
    // TODO: 干点啥
    val isGroup: Boolean
}

object PrefGroupInstance : PreferenceGroupScope {
    override val isGroup: Boolean = true
}

object PrefAreaInstance : PreferenceGroupScope {
    override val isGroup: Boolean = false
}