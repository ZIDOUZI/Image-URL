package zdz.bilicover.pref.core

import android.content.SharedPreferences
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.withContext
import zdz.bilicover.pref.core.core.Preference
import kotlin.coroutines.CoroutineContext

internal abstract class BasePreference<T>(
    override val key: String,
    private val sharedPreferences: SharedPreferences,
    private val coroutineContext: CoroutineContext
) : Preference<T> {
    
    override fun isSet() = sharedPreferences.contains(key)
    
    override fun isNotSet() = !sharedPreferences.contains(key)
    
    override fun delete() = sharedPreferences.edit().remove(key).apply()
    
    override suspend fun deleteAndCommit() =
        withContext(coroutineContext) { sharedPreferences.edit().remove(key).commit() }
    
    override fun asCollector() =
        FlowCollector<T> { value -> set(value) }
    
    override fun resetToDefault() = set(defaultValue)
    
}