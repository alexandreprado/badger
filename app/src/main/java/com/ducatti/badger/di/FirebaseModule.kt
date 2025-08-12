package com.ducatti.badger.di

import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FirebaseModule {
    companion object {

        @Provides
        fun firebaseDatabase(): FirebaseDatabase {
            return Firebase.database
        }
    }
}
