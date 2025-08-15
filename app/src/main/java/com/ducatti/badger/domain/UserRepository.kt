package com.ducatti.badger.domain

import com.ducatti.badger.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun addUser(user: User): Result<Void?>

    suspend fun getUser(id: String): Result<User?>

    suspend fun updateUser(user: User): Result<Void?>

    suspend fun removeUser(id: String)

    fun getUsers(): Result<Flow<List<User>?>>

    fun searchUsers(name: String): Flow<List<User>?>
}
