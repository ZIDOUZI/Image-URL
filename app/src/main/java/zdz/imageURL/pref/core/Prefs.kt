package zdz.imageURL.pref.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import zdz.imageURL.pref.core.core.PrefMaker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefs @Inject constructor(
    @ApplicationContext val context: Context
){
    private val maker = PrefMaker(context.getSharedPreferences("prefs", Context.MODE_PRIVATE))
    
    private var _theme by maker.string("theme", "null")
    var theme: Boolean?
        get() = when (_theme) {
            "false" -> false
            "true" -> true
            else -> null
        }
        set(value) {
            _theme = value.toString()
        }
    var transparent by maker.bool("transparent")
}