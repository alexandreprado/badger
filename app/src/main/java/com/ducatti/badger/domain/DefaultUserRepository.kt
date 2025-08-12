package com.ducatti.badger.domain

import com.ducatti.badger.data.DataSource
import com.ducatti.badger.data.model.User
import com.ducatti.badger.utils.clearLowercase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultUserRepository @Inject constructor(
    private val dataSource: DataSource
) : UserRepository {

    private val path = "users"

    override suspend fun addUser(user: User) {
        dataSource.add(path, user.copy(nameLowercase = user.name.clearLowercase()))
    }

    override suspend fun getUser(id: String) =
        dataSource.getOnce("$path/$id", User::class.java)

    override suspend fun updateUser(user: User) {
        dataSource.set(path, user.copy(nameLowercase = user.name.clearLowercase()))
    }

    override suspend fun removeUser(id: String) {
        dataSource.remove("$path/$id")
    }

    override fun getUsers(): Flow<List<User>?> =
        dataSource.observeList(path, orderByChild = "nameLowercase", User::class.java)

    override fun searchUsers(name: String): Flow<List<User>?> =
        dataSource.observeSearch(
            path,
            orderByChild = "nameLowercase",
            prefixLowercase = name.clearLowercase(),
            limit = 200,
            clazz = User::class.java
        )
}
