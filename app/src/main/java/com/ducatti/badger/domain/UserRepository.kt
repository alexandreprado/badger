package com.ducatti.badger.domain

import com.ducatti.badger.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun addUser(user: User)

    suspend fun getUser(id: String): User?

    suspend fun updateUser(user: User)

    suspend fun removeUser(id: String)

    fun getUsers(): Flow<List<User>?>

    fun searchUsers(name: String): Flow<List<User>?>
}
