package zdz.bilicover.pref.core.core

import android.content.SharedPreferences
import kotlinx.coroutines.GlobalScope.coroutineContext
import zdz.bilicover.pref.core.BooleanPreference
import zdz.bilicover.pref.core.StringPreference

class PrefMaker(
    val prefs: SharedPreferences,
) {
    
    fun bool(key: String, default: Boolean = false): PrefImpl<Boolean> =
        PrefImpl(BooleanPreference(key, default, prefs, coroutineContext))
    
    fun string(key: String, default: String = ""): PrefImpl<String> =
        PrefImpl(StringPreference(key, default, prefs, coroutineContext))
    
}