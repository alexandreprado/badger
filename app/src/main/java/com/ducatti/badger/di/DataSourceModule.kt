package com.ducatti.badger.di

import com.ducatti.badger.data.DataSource
import com.ducatti.badger.data.firebase.FirebaseDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindFirebaseDataSource(
        impl: FirebaseDataSource
    ): DataSource
}
