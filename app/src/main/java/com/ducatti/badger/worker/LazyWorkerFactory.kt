package com.ducatti.healthapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * A [WorkerFactory] that lazily creates workers using Hilt.
 *
 * This factory is necessary because HiltWorkerFactory cannot be injected directly
 * into the WorkManager configuration in the Application class, as it would create a
 * circular dependency.
 *
 * This factory defers the creation of the HiltWorkerFactory until it's actually needed,
 * thus avoiding the circular dependency.
 */
class LazyWorkerFactory : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val realFactory =
            EntryPoints.get(appContext, FactoryEntryPoint::class.java).hiltWorkerFactory
        return realFactory.createWorker(appContext, workerClassName, workerParameters)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FactoryEntryPoint {
        val hiltWorkerFactory: HiltWorkerFactory
    }
}
