package com.a4auth.sdk

import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

import okhttp3.*
import okio.IOException

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Main private constructor(context: Context) {
    private val context = context
    private val client = OkHttpClient()

    init {
        val capabilities = intArrayOf(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val transportTypes = intArrayOf(NetworkCapabilities.TRANSPORT_CELLULAR)
        alwaysPreferNetworksWith(capabilities, transportTypes)
    }

    private fun alwaysPreferNetworksWith(
        capabilities: IntArray,
        transportTypes: IntArray
    ) {
        val request = NetworkRequest.Builder()

        for (capability in capabilities) {
            request.addCapability(capability)
        }
        for (transportType in transportTypes) {
            request.addTransportType(transportType)
        }
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(request.build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        ConnectivityManager.setProcessDefaultNetwork(network)
                    } else {
                        connectivityManager.bindProcessToNetwork(network)
                    }
                } catch (e: IllegalStateException) {
                    Log.e(ContentValues.TAG, "ConnectivityManager.NetworkCallback.onAvailable: ", e)
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
        private var instance: Main? = null

        @Synchronized
        fun getInstance(context: Context): Main? {
            if (null == instance) {
                instance = Main(context)
            }
            return instance
        }

        @Synchronized
        fun getInstance(): Main? {
            checkNotNull(instance) {
                Main::class.java.simpleName +
                        " is not initialized, call getInstance(...) first"
            }
            return instance
        }

    }
}