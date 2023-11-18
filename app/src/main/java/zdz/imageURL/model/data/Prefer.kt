package zdz.imageURL.model.data

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.android.qualifiers.ApplicationContext
import zdz.imageURL.BuildConfig
import zdz.imageURL.R
import zdz.imageURL.utils.DataUnit
import zdz.libs.preferences.utils.boolean
import zdz.libs.preferences.utils.enum
import zdz.libs.preferences.utils.get
import zdz.libs.preferences.utils.string
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefer @Inject constructor(
    @ApplicationContext context: Context,
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
    val imageChooser by ds[false]
    val urlChooser by ds[false]
    val jumpChooser by ds[true]
    
    // 缓存图片查看, url打开, 自动跳转打开
    val chooseOpener = listOf(
        imageChooser to context.getString(R.string.image_chooser),
        urlChooser to context.getString(R.string.url_chooser),
        jumpChooser to context.getString(R.string.jump_chooser)
    )
    val preferredMimeType by ds.enum<Bitmap.CompressFormat>()
    
    val jm by ds.string()
    
    /**
     * @see [DataUnit][zdz.imageURL.utils.DataUnit]
     */
    val dataUnit by ds[8_000_000f]
}