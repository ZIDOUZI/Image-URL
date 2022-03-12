package zdz.bilicover.pref.core.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefImpl<T>(private val pref: Preference<T>) :
    ReadWriteProperty<Any, T>,
    Preference<T> by pref {
    override fun getValue(thisRef: Any, property: KProperty<*>) =
        pref.get()
    
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        pref.set(value)
    
    override fun resetToDefault() = pref.set(pref.defaultValue)
}