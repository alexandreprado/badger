package com.ducatti.badger.di

import com.ducatti.badger.domain.DefaultUserRepository
import com.ducatti.badger.domain.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserRepository(impl: DefaultUserRepository): UserRepository
}
