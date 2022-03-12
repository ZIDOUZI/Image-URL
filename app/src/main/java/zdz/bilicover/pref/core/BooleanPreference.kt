package zdz.bilicover.pref.core

import android.content.SharedPreferences
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class BooleanPreference(
    override val key: String,
    override val defaultValue: Boolean,
    private val sharedPreferences: SharedPreferences,
    private val coroutineContext: CoroutineContext
) : BasePreference<Boolean>(key, sharedPreferences, coroutineContext) {
    
    override fun get() = sharedPreferences.getBoolean(key, defaultValue)
    
    override fun set(value: Boolean) = sharedPreferences.edit().putBoolean(key, value).apply()
    
    override suspend fun setAndCommit(value: Boolean) = withContext(coroutineContext) {
        sharedPreferences.edit().putBoolean(key, value).commit()
    }
    
}