package ru.mirea.ivashechkinav.todo.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

class NetworkChangeReceiver(private val repository: TodoItemsRepository) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isConnectedToInternet(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.pullItemsFromServer()
            }
        }
    }

    private fun isConnectedToInternet(context: Context): Boolean {
        return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        }
    }
}
