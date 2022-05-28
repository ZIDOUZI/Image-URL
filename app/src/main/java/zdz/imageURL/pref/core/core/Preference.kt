package zdz.imageURL.pref.core.core

import kotlinx.coroutines.flow.FlowCollector

interface Preference<T>{
    
    val key: String
    
    val defaultValue: T
    
    fun get(): T
    
    fun set(value: T)
    
    suspend fun setAndCommit(value: T): Boolean
    
    fun isSet(): Boolean
    
    fun isNotSet(): Boolean
    
    fun delete()
    
    suspend fun deleteAndCommit(): Boolean
    
    fun asCollector(): FlowCollector<T>
    
    fun resetToDefault()
    
}