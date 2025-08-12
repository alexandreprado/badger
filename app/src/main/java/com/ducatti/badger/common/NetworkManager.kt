package com.ducatti.healthapp.common

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.ducatti.badger.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class monitors network availability and provides a way to check
 * if the device currently has an active internet connection.
 *
 * It uses [ConnectivityManager.NetworkCallback] to listen for network changes
 * and updates a [kotlinx.coroutines.flow.StateFlow] that can be observed by
 * other parts of the application.
 *
 * @param connectivityManager The system's [ConnectivityManager] instance, injected via Hilt.
 * @param applicationScope A [CoroutineScope] tied to the application's lifecycle,
 * used for managing the [kotlinx.coroutines.flow.StateFlow].
 */
@Singleton
class NetworkManager @Inject constructor(
    connectivityManager: ConnectivityManager,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    private val connectivityState = MutableStateFlow(true)

    val state = connectivityState
        .stateIn(applicationScope, SharingStarted.Lazily, true)

    fun hasConnectivity(): Boolean = connectivityState.value

    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        private val networks = mutableSetOf<Network>()

        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            networks += network
            connectivityState.value = true
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            networks -= network
            connectivityState.value = networks.isNotEmpty()
        }
    }

    init {
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}
