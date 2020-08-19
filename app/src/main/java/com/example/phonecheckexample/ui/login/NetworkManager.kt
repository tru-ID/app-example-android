package com.example.phonecheckexample.ui.login

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.*
import okio.IOException


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkManager private constructor(context: Context) {

    private val context = context
    private val client = OkHttpClient()

    init {
        // Add any NetworkCapabilities.NET_CAPABILITY_...
        val capabilities = intArrayOf(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        // Add any NetworkCapabilities.TRANSPORT_...
        val transportTypes = intArrayOf(NetworkCapabilities.TRANSPORT_CELLULAR)

        alwaysPreferNetworksWith(capabilities, transportTypes)
    }

    private fun alwaysPreferNetworksWith(
        capabilities: IntArray,
        transportTypes: IntArray
    ) {
        val request = NetworkRequest.Builder()

        // add capabilities
        for (cap in capabilities) {
            request.addCapability(cap)
        }

        // add transport types
        for (trans in transportTypes) {
            request.addTransportType(trans)
        }
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(request.build(), object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        ConnectivityManager.setProcessDefaultNetwork(network)
                    } else {
                        connectivityManager.bindProcessToNetwork(network)
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "ConnectivityManager.NetworkCallback.onAvailable: ", e)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                println("onLost: $network")
            }
            override fun onUnavailable() {
                super.onUnavailable()
                println("onUnavailable")
            }

        })
    }

    fun requestAsync(url: String, method: String, body: RequestBody?=null, callback: Callback) {

        val request = Request.Builder()
            .method(method, body)
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {

            var mainHandler: Handler = Handler(context.mainLooper)

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()

                mainHandler.post(Runnable {
                    callback.onFailure(call, e)
                })
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    println(response.body!!.string())

                    mainHandler.post(Runnable {
                        callback.onResponse(call, response)
                    })
                }
            }
        })

    }

    fun requestSync(url: String, method: String, body: RequestBody?=null): String {
        val request = Request.Builder()
            .method(method, body)
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val rawResponse = response.body!!.string()
            println("Response to $url")
            println(rawResponse)

            return rawResponse
        }
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