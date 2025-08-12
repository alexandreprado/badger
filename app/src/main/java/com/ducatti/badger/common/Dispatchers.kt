package com.ducatti.badger.common

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Custom qualifier annotation for injecting different dispatchers.
 *
 * This annotation is used in conjunction with Dagger Hilt to specify which
 * [AppDispatchers] should be injected for a particular dependency.
 *
 * @property appDispatcher The type of dispatcher to
 * inject (e.g., [AppDispatchers.Default], [AppDispatchers.IO]).
 */
@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val appDispatcher: AppDispatchers)

enum class AppDispatchers {
    Default,
    IO,
}
