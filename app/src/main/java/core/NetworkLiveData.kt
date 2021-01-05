package ro.ubbcluj.cs.ilazar.myapp2.core

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData

class NetworkLiveData internal constructor(private val connectivityManager: ConnectivityManager):LiveData<Boolean>(){
    companion object{
        var networkInfo:Boolean = true
    }
    constructor(application: Application) : this(application.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager)
    private val networkCallback =@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            postValue(true)
        }
        override fun onLost(network: Network) {
            postValue(false)
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActive() {
        super.onActive()
        Log.v(TAG, "on Active ")
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetwork?.isConnected == true)
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onInactive() {
        super.onInactive()
        Log.v(TAG, "on inactive ")
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

}