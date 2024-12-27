package nl.newlin.abnamro.assesment

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.newlin.abnamro.assesment.data.GithubRepoDatasource
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(val datasource: GithubRepoDatasource, val applicationContext: Context): ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    private val connectivityManager =
        applicationContext.getSystemService(ConnectivityManager::class.java)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isNetworkAvailable.value = true
        }

        override fun onLost(network: Network) {
            _isNetworkAvailable.value = false
        }
    }

    init {
        val networkRequest = android.net.NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun loadCachedData() {
        viewModelScope.launch(Dispatchers.IO) {
            datasource.loadCachedData()
        }
    }

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            datasource.syncRepositories()
        }
    }



}