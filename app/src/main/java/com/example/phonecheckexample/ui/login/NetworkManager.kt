package com.example.phonecheckexample.ui.login

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkManager private constructor(context: Context) {

    private val context = context
    private val client = OkHttpClient()

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

        var connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = NetworkRequest.Builder()
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)//TRANSPORT_WIFI
        val build = builder.build()
        connectivityManager.requestNetwork(build, networkCallback)
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

            return response.body!!.string()
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