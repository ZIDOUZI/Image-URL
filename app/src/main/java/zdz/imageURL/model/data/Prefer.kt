package zdz.imageURL.model.data

import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.launch
import zdz.imageURL.BuildConfig
import zdz.imageURL.utils.DataUnit
import zdz.libs.preferences.model.PreferenceIOScope
import zdz.libs.preferences.utils.boolean
import zdz.libs.preferences.utils.enum
import zdz.libs.preferences.utils.get
import zdz.libs.preferences.utils.string
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefer @Inject constructor(
    private val ds: DataStore<Preferences>,
) {
    val darkTheme by ds.boolean()
    val alpha by ds[1f]
    val firstLink by ds[false]
    val secondLink by ds[true]
    val autoCheck by ds[!BuildConfig.DEBUG]
    val advanced by ds[false]
    val preferredID by ds[Type.Companion.TypeSerializer]
    val autoJump by ds[true]
    val closeAfterProcess by ds[false]
    val chooseOpener by ds[false]
    val preferredMimeType by ds.enum<Bitmap.CompressFormat>()
    
    val jm by ds.string()
    
    /**
     * @see [DataUnit][zdz.imageURL.utils.DataUnit]
     */
    val dataUnit by ds[8000_000f]
    
    fun resetAll() = PreferenceIOScope.launch { ds.edit(MutablePreferences::clear) }
}