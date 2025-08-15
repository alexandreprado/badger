package com.ducatti.badger.data.firebase

import com.ducatti.badger.common.AppDispatchers
import com.ducatti.badger.common.Dispatcher
import com.ducatti.badger.data.DataSource
import com.ducatti.badger.data.model.IdModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val db: FirebaseDatabase,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : DataSource {

    private fun ref(path: String): DatabaseReference = db.getReference(path)

    override suspend fun <T> add(path: String, value: T): Void? =
        withContext(ioDispatcher) {
            ref(path).push().setValue(value).await()
        }

    override suspend fun <T : Any?> set(path: String, value: T): Void? =
        withContext(ioDispatcher) {
            ref(path).setValue(value).await()
        }

    override suspend fun update(path: String, values: Map<String, Any?>): Void? =
        withContext(ioDispatcher) {
            ref(path).updateChildren(values).await()
        }

    override fun <T : Any> observe(path: String, clazz: Class<T>): Flow<T?> =
        ref(path).asSnapshotFlow()
            .map { snap ->
                snap.getValue(clazz)?.also { obj ->
                    if (obj is IdModel) obj.id = snap.key.orEmpty()
                }
            }
            .distinctUntilChanged()
            .conflate()

    override suspend fun <T : Any> getOnce(
        path: String,
        clazz: Class<T>
    ): T? {
        val snap = ref(path).get().await()
        return snap.getValue(clazz)?.also { obj ->
            if (obj is IdModel) obj.id = snap.key.orEmpty()
        }
    }

    override fun <T : Any> observeList(
        path: String,
        orderByChild: String?,
        clazz: Class<T>
    ): Flow<List<T>> =
        ref(path)
            .orderBy(orderByChild)
            .asSnapshotFlow()
            .map { snap ->
                snap.children.mapNotNull { child ->
                    child.getValue(clazz)?.also { obj ->
                        if (obj is IdModel) obj.id = child.key.orEmpty()
                    }
                }
            }

    override suspend fun <T : Any> getOnceList(
        path: String,
        orderByChild: String?,
        clazz: Class<T>
    ): List<T> =
        ref(path)
            .orderBy(orderByChild)
            .get()
            .await()
            .children
            .mapNotNull { child ->
                child.getValue(clazz)?.also { obj ->
                    if (obj is IdModel) obj.id = child.key.orEmpty()
                }
            }

    override suspend fun remove(path: String) {
        ref(path).removeValue().await()
    }

    private fun DatabaseReference.orderBy(orderByChild: String?): Query {
        return if (orderByChild != null) {
            searchQuery(orderByChild)
        } else {
            this
        }
    }

    private fun DatabaseReference.searchQuery(
        orderByChild: String,
        startAt: String? = null,
        endAt: String? = null,
        equalTo: String? = null,
        limitToFirst: Int? = null,
        limitToLast: Int? = null
    ): Query {
        var q: Query = orderByChild(orderByChild)
        if (equalTo != null) {
            q = q.equalTo(equalTo)
        } else {
            if (startAt != null) q = q.startAt(startAt)
            if (endAt != null) q = q.endAt(endAt)
        }
        if (limitToFirst != null) q = q.limitToFirst(limitToFirst)
        if (limitToLast != null) q = q.limitToLast(limitToLast)
        return q
    }

    fun <T : Any> observeSearch(
        path: String,
        orderByChild: String,
        startAt: String?,
        endAt: String?,
        equalTo: String?,
        limitToFirst: Int?,
        limitToLast: Int?,
        clazz: Class<T>
    ): Flow<List<T>> =
        ref(path)
            .searchQuery(orderByChild, startAt, endAt, equalTo, limitToFirst, limitToLast)
            .asSnapshotFlow()
            .map { snap ->
                snap.children.mapNotNull { child ->
                    child.getValue(clazz)?.also { obj ->
                        if (obj is IdModel) obj.id = child.key.orEmpty()
                    }
                }
            }
            .distinctUntilChanged()
            .conflate()

    override fun <T : Any> observeSearch(
        path: String,
        orderByChild: String,
        prefixLowercase: String,
        limit: Int?,
        clazz: Class<T>
    ): Flow<List<T>> =
        observeSearch(
            path = path,
            orderByChild = orderByChild,
            startAt = prefixLowercase,
            endAt = prefixLowercase + upperBounds,
            equalTo = null,
            limitToFirst = limit,
            limitToLast = null,
            clazz = clazz
        )

    private fun Query.asSnapshotFlow(): Flow<DataSnapshot> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                trySend(s).isSuccess
            }

            override fun onCancelled(e: DatabaseError) {
                close(e.toException())
            }
        }
        addValueEventListener(listener)
        awaitClose { removeEventListener(listener) }
    }.buffer(Channel.CONFLATED)

    private val upperBounds = "\uf8ff"
}
