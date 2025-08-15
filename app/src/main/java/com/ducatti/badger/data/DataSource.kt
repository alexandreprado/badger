package com.ducatti.badger.data

import kotlinx.coroutines.flow.Flow

interface DataSource {
    suspend fun <T : Any?> add(path: String, value: T): Void?
    suspend fun <T : Any?> set(path: String, value: T): Void?
    suspend fun update(path: String, values: Map<String, Any?>): Void?
    fun <T : Any> observe(path: String, clazz: Class<T>): Flow<T?>
    suspend fun <T : Any> getOnce(path: String, clazz: Class<T>): T?
    fun <T : Any> observeList(path: String, orderByChild: String?, clazz: Class<T>): Flow<List<T>>
    suspend fun <T : Any> getOnceList(path: String, orderByChild: String?, clazz: Class<T>): List<T>
    suspend fun remove(path: String)

    fun <T : Any> observeSearch(
        path: String,
        orderByChild: String,
        prefixLowercase: String,
        limit: Int? = null,
        clazz: Class<T>
    ): Flow<List<T>>
}
