package com.ducatti.badger

import android.app.Application
import androidx.work.Configuration
import com.ducatti.healthapp.worker.LazyWorkerFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    /**
     * Provides a custom [Configuration] for [androidx.work.WorkManager]
     * using a lazy worker factory
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(LazyWorkerFactory())
            .build()
}
