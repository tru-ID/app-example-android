package com.example.phonecheckexample.ui.login

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkManager private constructor(context: Context) {

    //for Volley API
    var queue: RequestQueue

    init {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                //Use the network to do your thing
                println("onAvailable: $network")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                println("onLost: $network")
            }
            override fun onUnavailable() {
                super.onUnavailable()
                println("onUnavailable")
            }
        }

        queue = Volley.newRequestQueue(context.applicationContext)

        var connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = NetworkRequest.Builder()
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)//TRANSPORT_WIFI
        val build = builder.build()
        connectivityManager!!.requestNetwork(build, networkCallback)
    }

    fun <T> request(request: Request<T>?): Request<T?>? {
        return queue.add(request)
    }

    companion object {

        private var instance: NetworkManager? = null

        @Synchronized
        fun getInstance(context: Context): NetworkManager? {
            if (null == instance) {
                instance = NetworkManager(context)
            }
            return instance
        }

        //this is so you don't need to pass context each time
        @Synchronized
        fun getInstance(): NetworkManager? {
            checkNotNull(instance) {
                NetworkManager::class.java.simpleName +
                        " is not initialized, call getInstance(...) first"
            }
            return instance
        }

    }
}