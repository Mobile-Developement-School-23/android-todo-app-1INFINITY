package ru.mirea.ivashechkinav.todo.presentation.receiver

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NetworkChangeReceiver(applicationContext: Context) {

    private val _networkStateFlow = MutableSharedFlow<Boolean>()
    val stateFlow = _networkStateFlow.asSharedFlow()

    private val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            CoroutineScope(Dispatchers.IO).launch {
                _networkStateFlow.emit(true)
            }
        }

        override fun onLost(network: Network) {
            CoroutineScope(Dispatchers.IO).launch {
                _networkStateFlow.emit(false)
            }
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }
}
